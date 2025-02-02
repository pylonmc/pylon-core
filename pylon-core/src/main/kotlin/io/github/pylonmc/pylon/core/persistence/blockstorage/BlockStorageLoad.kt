package io.github.pylonmc.pylon.core.persistence.blockstorage

import com.github.shynixn.mccoroutine.bukkit.launch
import io.github.pylonmc.pylon.core.block.position
import io.github.pylonmc.pylon.core.event.PylonBlockLoadEvent
import io.github.pylonmc.pylon.core.event.PylonChunkBlocksLoadEvent
import io.github.pylonmc.pylon.core.pluginInstance
import kotlinx.coroutines.Dispatchers
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkLoadEvent

internal object BlockStorageLoad : Listener {
    private val commitDispatcher = Dispatchers.Default

    @EventHandler
    private fun onChunkLoad(event: ChunkLoadEvent) {
        val chunkPosition = event.chunk.position

        Bukkit.getLogger().severe("SCHEDULE LOAD ${chunkPosition}")

        pluginInstance.launch(commitDispatcher) {
            val blocks = BlockStorage.readChunkFromDisk(chunkPosition)

            BlockStorage.addChunkToCache(chunkPosition, blocks)

            Bukkit.getLogger().severe("COMMIT LOAD ${chunkPosition}")

            Bukkit.getScheduler().runTask(pluginInstance, Runnable {
                PylonChunkBlocksLoadEvent(chunkPosition.chunk!!, blocks).callEvent()

                for (block in blocks) {
                    PylonBlockLoadEvent(block.block, block).callEvent()
                }
            })
        }
    }
}