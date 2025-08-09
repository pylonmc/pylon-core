package io.github.pylonmc.pylon.core.block.waila

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.block.BlockStorage
import io.github.pylonmc.pylon.core.config.PylonConfig
import io.github.pylonmc.pylon.core.entity.EntityStorage
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
            val targetBlock = player.rayTraceBlocks(blockReach)?.hitBlock?.let(BlockStorage::get)
            if (targetBlock != null) {
                try {
                    val config = targetBlock.getWaila(player)
                    config.apply(bossbar)
                    on()
                } catch (e: Exception) {
                    e.printStackTrace()
                    off()
                }
            } else {
                off()
            }
        }
    }

    companion object : Listener {

        private val wailaKey = pylonKey("waila")
        private val wailas = mutableMapOf<UUID, Waila>()

        /**
         * Forcibly adds a WAILA display for the given player
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
         * Forcibly removes a WAILA display for the given player
         */
        @JvmStatic
        fun removePlayer(player: Player) {
            wailas.remove(player.uniqueId)?.destroy()
        }

        @get:JvmStatic
        @set:JvmStatic
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