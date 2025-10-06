package io.github.pylonmc.pylon.core.block.waila

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.block.BlockStorage
import io.github.pylonmc.pylon.core.config.PylonConfig
import io.github.pylonmc.pylon.core.entity.EntityStorage
import io.github.pylonmc.pylon.core.util.position.BlockPosition
import io.github.pylonmc.pylon.core.util.position.position
import io.github.pylonmc.pylon.core.util.pylonKey
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.persistence.PersistentDataType
import java.util.UUID

/**
 * Handles WAILAs (the text that displays a block's name when looking
 * at the block).
 *
 * You should not need to use this if you just want to change the WAILA of
 * a [io.github.pylonmc.pylon.core.block.PylonBlock]. For that, see
 * [io.github.pylonmc.pylon.core.block.PylonBlock.getWaila]
 */
class Waila private constructor(private val player: Player, private val job: Job) {

    private val bossbar = BossBar.bossBar(
        Component.empty(),
        1F,
        BossBar.Color.WHITE,
        BossBar.Overlay.PROGRESS
    )

    private fun on() {
        val bossbars = player.activeBossBars()
        for (bossbar in bossbars) {
            player.hideBossBar(bossbar)
        }
        player.showBossBar(this.bossbar)
        for (bossbar in bossbars) {
            player.showBossBar(bossbar)
        }
    }

    private fun off() {
        player.hideBossBar(bossbar)
    }

    private fun destroy() {
        off()
        job.cancel()
    }

    private fun updateDisplay() {
        val entityReach = player.getAttribute(Attribute.ENTITY_INTERACTION_RANGE)?.value ?: 3
        val targetEntity = player.rayTraceEntities(entityReach.toInt())?.hitEntity
        if (targetEntity != null) {
            val config = try {
                EntityStorage.get(targetEntity)?.getWaila(player)
            } catch (e: Exception) {
                e.printStackTrace()
                off()
                return
            }
            if (config != null) {
                config.apply(bossbar)
                on()
            } else {
                off()
            }
        } else {
            val blockReach = player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE)?.value ?: 4.5
            val block = player.rayTraceBlocks(blockReach)?.hitBlock
            if (block == null) {
                off()
                return
            }
            val config = try {
                overrides[block.position]?.invoke(player) ?: block.let(BlockStorage::get)?.getWaila(player)
            } catch (e: Exception) {
                e.printStackTrace()
                off()
                return
            }
            if (config != null) {
                config.apply(bossbar)
                on()
            } else {
                off()
            }
        }
    }

    companion object : Listener {

        private val wailaKey = pylonKey("waila")
        private val wailas = mutableMapOf<UUID, Waila>()

        private val overrides = mutableMapOf<BlockPosition, (Player) -> WailaConfig?>()

        /**
         * Forcibly adds a WAILA display for the given player.
         */
        @JvmStatic
        fun addPlayer(player: Player) {
            wailas[player.uniqueId] = Waila(player, PylonCore.launch {
                delay(1.ticks)
                val waila = wailas[player.uniqueId]!!
                while (true) {
                    waila.updateDisplay()
                    delay(PylonConfig.wailaTickInterval.ticks)
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
        var Player.wailaEnabled: Boolean
            get() = this.persistentDataContainer.getOrDefault(wailaKey, PersistentDataType.BOOLEAN, true)
            set(value) {
                this.persistentDataContainer.set(wailaKey, PersistentDataType.BOOLEAN, value)
                if (value) {
                    addPlayer(this)
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
        fun addWailaOverride(position: BlockPosition, provider: (Player) -> WailaConfig?) {
            overrides[position] = provider
        }

        @JvmStatic
        fun removeWailaOverride(position: BlockPosition) {
            overrides.remove(position)
        }

        @EventHandler(priority = EventPriority.MONITOR)
        private fun onPlayerJoin(event: PlayerJoinEvent) {
            val player = event.player
            if (player.wailaEnabled) {
                addPlayer(player)
            }
        }

        @EventHandler(priority = EventPriority.MONITOR)
        private fun onPlayerQuit(event: PlayerQuitEvent) {
            removePlayer(event.player)
        }
    }
}