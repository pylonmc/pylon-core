package io.github.pylonmc.pylon.core.block.waila

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.block.BlockStorage
import io.github.pylonmc.pylon.core.config.PylonConfig
import io.github.pylonmc.pylon.core.util.pylonKey
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import org.bukkit.Bukkit
import org.bukkit.attribute.Attribute
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.persistence.PersistentDataType
import java.util.UUID

class Waila private constructor(player: Player, private val job: Job) {

    private val bossbar = Bukkit.createBossBar(null, BarColor.RED, BarStyle.SOLID)

    init {
        bossbar.isVisible = false
        bossbar.addPlayer(player)
    }

    fun destroy() {
        job.cancel()
        bossbar.removeAll()
    }

    companion object : Listener {

        private val wailaKey = pylonKey("walia")
        private val walias = mutableMapOf<UUID, Waila>()

        @JvmStatic
        fun addPlayer(player: Player) {
            walias[player.uniqueId] = Waila(player, PylonCore.launch {
                delay(1.ticks)
                while (true) {
                    val walia = walias[player.uniqueId]!!
                    val reach = player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE)?.value ?: 4.5
                    val lookingAt = player.rayTraceBlocks(reach)?.hitBlock?.let(BlockStorage::get)
                    if (lookingAt != null) {
                        val config = lookingAt.getWaila(player)
                        config.apply(walia.bossbar)
                        walia.bossbar.isVisible = true
                    } else {
                        walia.bossbar.isVisible = false
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
        fun isWailaEnabled(player: Player): Boolean {
            return player.persistentDataContainer.getOrDefault(wailaKey, PersistentDataType.BOOLEAN, true)
        }

        @JvmStatic
        fun setWailaEnabled(player: Player, enabled: Boolean) {
            player.persistentDataContainer.set(wailaKey, PersistentDataType.BOOLEAN, enabled)
            if (enabled) {
                addPlayer(player)
            } else {
                removePlayer(player)
            }
        }

        @EventHandler(priority = EventPriority.MONITOR)
        private fun onPlayerJoin(event: PlayerJoinEvent) {
            val player = event.player
            if (isWailaEnabled(player)) {
                addPlayer(player)
            }
        }

        @EventHandler(priority = EventPriority.MONITOR)
        private fun onPlayerQuit(event: PlayerQuitEvent) {
            removePlayer(event.player)
        }
    }
}