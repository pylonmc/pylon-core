package io.github.pylonmc.pylon.core.persistence

import io.github.pylonmc.pylon.core.block.position
import io.github.pylonmc.pylon.core.pluginInstance
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.ChunkUnloadEvent

internal object BlockStorageListener : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(BlockStorageListener, pluginInstance)
    }

    @EventHandler
    fun onChunkLoad(event: ChunkLoadEvent) {
        BlockStorage.load(event.chunk.position)
    }

    @EventHandler
    fun onChunkUnload(event: ChunkUnloadEvent) {
        BlockStorage.save(event.chunk.position)
    }
}