package io.github.pylonmc.pylon.core.persistence

import io.github.pylonmc.pylon.core.block.position
import io.github.pylonmc.pylon.core.pluginInstance
import org.bukkit.Bukkit
import org.bukkit.ExplosionResult
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockBurnEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.BlockFadeEvent
import org.bukkit.event.block.BlockFormEvent
import org.bukkit.event.block.BlockFromToEvent
import org.bukkit.event.block.BlockPistonExtendEvent
import org.bukkit.event.block.BlockPistonRetractEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.ChunkUnloadEvent

internal object BlockStorageListener : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(BlockStorageListener, pluginInstance)
    }

    @EventHandler
    fun chunkLoad(event: ChunkLoadEvent) {
        BlockStorage.load(event.chunk.position)
    }

    @EventHandler
    fun chunkSave(event: ChunkUnloadEvent) {
        BlockStorage.save(event.chunk.position)
    }

    @EventHandler
    fun blockRemove(event: BlockBreakEvent) {
        BlockStorage.remove(event.block.position)
    }

    @EventHandler
    fun blockBurn(event: BlockBurnEvent) {
        BlockStorage.remove(event.block.position)
    }

    @EventHandler
    fun blockRemove(event: BlockExplodeEvent) {
        BlockStorage.remove(event.block.position)
        for (block in event.blockList()) {
            BlockStorage.remove(block.position)
        }
    }

    @EventHandler
    fun blockRemove(event: EntityExplodeEvent) {
        for (block in event.blockList()) {
            BlockStorage.remove(block.position)
        }
    }

    @EventHandler
    fun blockRemove(event: BlockFadeEvent) {
        BlockStorage.remove(event.block.position)
    }

    @EventHandler
    fun disallowForming(event: BlockFormEvent) {
        if (BlockStorage.exists(event.block.position)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun disallowFromTo(event: BlockFromToEvent) {
        if (BlockStorage.exists(event.block.position)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun disallowMovementByPistons(event: BlockPistonExtendEvent) {
        for (block in event.blocks) {
            if (BlockStorage.exists(block.position)) {
                event.isCancelled = true
                return
            }
        }
    }

    @EventHandler
    fun disallowMovementByPistons(event: BlockPistonRetractEvent) {
        for (block in event.blocks) {
            if (BlockStorage.exists(block.position)) {
                event.isCancelled = true
                return
            }
        }
    }
}