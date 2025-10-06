package io.github.pylonmc.pylon.core.waila

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.block.BlockStorage
import io.github.pylonmc.pylon.core.block.PylonBlock
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.entity.EntityStorage
import io.github.pylonmc.pylon.core.entity.PylonEntity
import io.github.pylonmc.pylon.core.event.PylonBlockWailaEvent
import io.github.pylonmc.pylon.core.event.PylonEntityWailaEvent
import io.github.pylonmc.pylon.core.i18n.PylonArgument
import io.github.pylonmc.pylon.core.util.position.BlockPosition
import io.github.pylonmc.pylon.core.util.position.position
import io.github.pylonmc.pylon.core.util.pylonKey
import io.papermc.paper.raytracing.RayTraceTarget
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.UUID
import kotlin.math.max

/**
 * Handles WAILAs (the text that displays a block's name when looking
 * at the block).
 *
 * If you want to change the WAILA display for your [PylonBlock] or [PylonEntity], see
 * [PylonBlock.getWaila] and [PylonEntity.getWaila], if you need to change the WAILA
 * display for a different block/entity, see [addWailaOverride].
 */
class Waila private constructor(private val player: Player, playerConfig: PlayerWailaConfig, private val job: Job) {

    private var config = playerConfig
        set(value) {
            if (field.type != value.type) {
                hide()
            }
            field = value
        }

    private val bossBar = BossBar.bossBar(
        Component.empty(),
        WailaConfig.defaultDisplay.progress,
        WailaConfig.defaultDisplay.color,
        WailaConfig.defaultDisplay.overlay
    )

    private fun send(display: WailaDisplay) {
        when (config.type) {
            Type.BOSSBAR -> {
                player.hideBossBar(bossBar)
                val color = if (display.color in WailaConfig.allowedBossBarColors) {
                    display.color
                } else {
                    WailaConfig.defaultDisplay.color
                }
                val overlay = if (display.overlay in WailaConfig.allowedBossBarOverlays) {
                    display.overlay
                } else {
                    WailaConfig.defaultDisplay.overlay
                }

                bossBar.name(display.text)
                bossBar.color(color)
                bossBar.overlay(overlay)
                bossBar.progress(display.progress)
                player.showBossBar(bossBar)
            }
            Type.ACTIONBAR -> player.sendActionBar(display.text)
        }
    }

    private fun hide() {
        when (config.type) {
            Type.BOSSBAR -> {
                if (player.activeBossBars().contains(bossBar)) {
                    player.hideBossBar(bossBar)
                }
            }
            Type.ACTIONBAR -> player.sendActionBar(Component.empty())
        }
    }

    private fun destroy() {
        hide()
        job.cancel()
    }

    private fun updateDisplay() {
        val entityReach = player.getAttribute(Attribute.ENTITY_INTERACTION_RANGE)?.value ?: 3.0
        val blockReach = player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE)?.value ?: 4.5

        val rayTraceResult = player.world.rayTrace { builder ->
            builder.start(player.eyeLocation)
            builder.direction(player.eyeLocation.direction)
            builder.maxDistance(max(entityReach, blockReach))
            builder.entityFilter { entity -> entity != player && entity.location.distanceSquared(player.location) <= entityReach * entityReach }
            builder.blockFilter { block -> block.location.distanceSquared(player.location) <= blockReach * blockReach }
            builder.targets(RayTraceTarget.ENTITY, RayTraceTarget.BLOCK)
        }

        if (rayTraceResult == null) {
            hide()
            return
        }

        rayTraceResult.hitEntity?.let { entity ->
            try {
                var display = entityOverrides[entity.uniqueId]?.invoke(player)
                    ?: entity.let(EntityStorage::get)?.getWaila(player)

                if (display == null && player.wailaConfig.vanillaWailaEnabled) {
                    display = WailaDisplay(Component.translatable(entity.type.translationKey()))
                }

                if (display != null) {
                    val event = PylonEntityWailaEvent(player, entity, display)
                    event.callEvent()
                    if (!event.isCancelled && event.display != null) {
                        send(event.display!!)
                    } else {
                        hide()
                    }
                } else {
                    hide()
                }
            } catch(e: Exception) {
                e.printStackTrace()
                hide()
            }
        }

        rayTraceResult.hitBlock?.let { block ->
            try {
                var display = blockOverrides[block.position]?.invoke(player)
                    ?: block.let(BlockStorage::get)?.getWaila(player)

                if (display == null && player.wailaConfig.vanillaWailaEnabled) {
                    display = WailaDisplay(Component.translatable(block.type.translationKey()))
                }

                if (display != null) {
                    val event = PylonBlockWailaEvent(player, block, display)
                    event.callEvent()
                    if (!event.isCancelled && event.display != null) {
                        send(event.display!!)
                    } else {
                        hide()
                    }
                } else {
                    hide()
                }
            } catch(e: Exception) {
                e.printStackTrace()
                hide()
            }
        }
    }

    enum class Type {
        BOSSBAR,
        ACTIONBAR
    }

    companion object : Listener {

        private val wailaKey = pylonKey("waila")
        private val wailas = mutableMapOf<UUID, Waila>()

        private val blockOverrides = mutableMapOf<BlockPosition, (Player) -> WailaDisplay?>()
        private val entityOverrides = mutableMapOf<UUID, (Player) -> WailaDisplay?>()

        /**
         * Forcibly adds a WAILA display for the given player.
         */
        @JvmStatic
        fun addPlayer(player: Player, config: PlayerWailaConfig = player.wailaConfig) {
            if (wailas.containsKey(player.uniqueId) || !config.enabled) {
                return
            }

            wailas[player.uniqueId] = Waila(player, config, PylonCore.launch {
                delay(1.ticks)
                val waila = wailas[player.uniqueId]!!
                while (true) {
                    waila.updateDisplay()
                    delay(WailaConfig.tickInterval.ticks)
                }
            })
        }

        /**
         * Forcibly removes a WAILA display for the given player.
         */
        @JvmStatic
        fun removePlayer(player: Player) {
            wailas.remove(player.uniqueId)?.destroy()
        }

        @JvmStatic
        var Player.wailaConfig: PlayerWailaConfig
            get() = this.persistentDataContainer.getOrDefault(wailaKey, PylonSerializers.PLAYER_WAILA_CONFIG, PlayerWailaConfig()).apply {
                player = this@wailaConfig
                if (!WailaConfig.enabledTypes.contains(type)) {
                    sendMessage(Component.translatable("pylon.pyloncore.message.waila.type-disabled").arguments(
                        PylonArgument.of("type", type.name.lowercase())
                    ))
                    type = WailaConfig.defaultType
                }
            }
            set(value) {
                this.persistentDataContainer.set(wailaKey, PylonSerializers.PLAYER_WAILA_CONFIG, value)
                if (value.enabled) {
                    if (!wailas.containsKey(uniqueId)) {
                        addPlayer(this, value)
                    } else {
                        wailas[this.uniqueId]?.config = value
                    }
                } else {
                    removePlayer(this)
                }
            }

        /**
         * Adds a WAILA override for the given position. This will always show the
         * provided WAILA config when a WAILA-enabled player looks at the block at
         * the given position, regardless of the block type or even if the block is
         * not a Pylon block.
         */
        @JvmStatic
        fun addWailaOverride(position: BlockPosition, provider: (Player) -> WailaDisplay?) {
            blockOverrides[position] = provider
        }

        @JvmStatic
        fun addWailaOverride(entity: Entity, provider: (Player) -> WailaDisplay?) {
            entityOverrides[entity.uniqueId] = provider
        }

        @JvmStatic
        fun removeWailaOverride(position: BlockPosition) {
            blockOverrides.remove(position)
        }

        @JvmStatic
        fun removeWailaOverride(entity: Entity) {
            entityOverrides.remove(entity.uniqueId)
        }

        @EventHandler(priority = EventPriority.MONITOR)
        private fun onPlayerJoin(event: PlayerJoinEvent) {
            val player = event.player

            // TODO: Remove this migration code in a future version
            if (player.persistentDataContainer.has(wailaKey, PylonSerializers.BOOLEAN)) {
                player.persistentDataContainer.remove(wailaKey)
            }

            if (player.wailaConfig.enabled) {
                addPlayer(player)
            }
        }

        @EventHandler(priority = EventPriority.MONITOR)
        private fun onPlayerQuit(event: PlayerQuitEvent) {
            removePlayer(event.player)
        }
    }
}