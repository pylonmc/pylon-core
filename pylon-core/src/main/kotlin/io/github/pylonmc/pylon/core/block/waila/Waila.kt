package io.github.pylonmc.pylon.core.block.waila

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import io.github.pylonmc.pylon.core.block.BlockStorage
import io.github.pylonmc.pylon.core.config.PylonConfig
import io.github.pylonmc.pylon.core.pluginInstance
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

    private val bossbar = BossBar.bossBar(Component.empty(), 1F, BossBar.Color.WHITE, BossBar.Overlay.PROGRESS)

    fun on() {
        bossbar.addViewer(player)
    }

    fun off() {
        bossbar.removeViewer(player)
    }

    fun destroy() {
        job.cancel()
        bossbar.removeViewer(player)
    }

    companion object : Listener {

        private val wailaKey = pylonKey("walia")
        private val walias = mutableMapOf<UUID, Waila>()

        @JvmStatic
        fun addPlayer(player: Player) {
            walias[player.uniqueId] = Waila(player, pluginInstance.launch {
                delay(1.ticks)
                val walia = walias[player.uniqueId]!!
                walia.on()
                while (true) {
                    val reach = player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE)?.value ?: 4.5
                    val lookingAt = player.rayTraceBlocks(reach)?.hitBlock?.let(BlockStorage::get)
                    if (lookingAt != null) {
                        val config = lookingAt.getWaila(player)
                        config.apply(walia.bossbar)
                        walia.on()
                    } else {
                        walia.off()
                    }
                    delay(PylonConfig.waliaInterval.ticks)
                }
            })
        }

        @JvmStatic
        fun removePlayer(player: Player) {
            walias.remove(player.uniqueId)?.destroy()
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