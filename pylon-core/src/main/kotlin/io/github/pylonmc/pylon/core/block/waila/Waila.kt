package io.github.pylonmc.pylon.core.block.waila

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import io.github.pylonmc.pylon.core.block.PylonBlock
import io.github.pylonmc.pylon.core.config.PylonConfig
import io.github.pylonmc.pylon.core.persistence.blockstorage.BlockStorage
import io.github.pylonmc.pylon.core.pluginInstance
import io.github.pylonmc.pylon.core.util.pylonKey
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import org.bukkit.Bukkit
import org.bukkit.attribute.Attribute
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import java.util.UUID

class Waila private constructor(private val player: Player, private val job: Job) {

    private val bossbar = Bukkit.createBossBar("", BarColor.RED, BarStyle.SOLID)

    init {
        bossbar.isVisible = false
        bossbar.progress = 1.0
        bossbar.addPlayer(player)
    }

    private fun show(block: PylonBlock<*>) {

    }

    companion object {

        private val wailaKey = pylonKey("walia")
        private val walias = mutableMapOf<UUID, Waila>()

        fun addPlayer(player: Player) {
            walias[player.uniqueId] = Waila(player, pluginInstance.launch {
                while (true) {
                    val walia = walias[player.uniqueId] ?: break
                    val reach = player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE)?.value ?: 4.5
                    val lookingAt = player.rayTraceBlocks(reach)?.hitBlock?.let(BlockStorage::get)
                    if (lookingAt != null) {
                        walia.show(lookingAt)
                    } else {
                        walia.bossbar.isVisible = false
                    }
                    delay(PylonConfig.waliaInterval.ticks)
                }
            })
        }

        fun removePlayer(player: Player) {
            walias.remove(player.uniqueId)?.job?.cancel()
        }

        fun isWailaEnabled(player: Player): Boolean {
            return player.persistentDataContainer.getOrDefault(wailaKey, PersistentDataType.BOOLEAN, true)
        }

        fun setWailaEnabled(player: Player, enabled: Boolean) {
            player.persistentDataContainer.set(wailaKey, PersistentDataType.BOOLEAN, enabled)
        }
    }
}