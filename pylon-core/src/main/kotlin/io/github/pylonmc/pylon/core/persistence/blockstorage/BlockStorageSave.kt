package io.github.pylonmc.pylon.core.persistence.blockstorage

import com.github.shynixn.mccoroutine.bukkit.launch
import io.github.pylonmc.pylon.core.block.position
import io.github.pylonmc.pylon.core.event.PylonBlockUnloadEvent
import io.github.pylonmc.pylon.core.event.PylonChunkBlocksUnloadEvent
import io.github.pylonmc.pylon.core.pluginInstance
import kotlinx.coroutines.Dispatchers
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkUnloadEvent

internal object BlockStorageSave : Listener {
    private val commitDispatcher = Dispatchers.Default


    @EventHandler
    private fun onChunkUnload(event: ChunkUnloadEvent) {
        val chunkPosition = event.chunk.position

        Bukkit.getLogger().severe("SCHEDULE SAVE ${chunkPosition}")

        // We immediately delete the chunk's blocks from loaded blocks, and then schedule the chunk
        // to be saved. This is to avoid a situation where a chunk has been unloaded, but its blocks
        // are still loaded in BlockStorage.
        val blocks = BlockStorage.removeChunkFromCache(chunkPosition)

        pluginInstance.launch(commitDispatcher) {
            BlockStorage.writeChunkToDisk(chunkPosition, blocks)

            Bukkit.getLogger().severe("COMMIT SAVE ${chunkPosition}")

            Bukkit.getScheduler().runTask(pluginInstance, Runnable {
                PylonChunkBlocksUnloadEvent(chunkPosition.chunk!!, blocks).callEvent()

                for (block in blocks) {
                    PylonBlockUnloadEvent(block.block, block).callEvent()
                }
            })
        }
    }
}