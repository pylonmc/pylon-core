package io.github.pylonmc.pylon.core.block.base

import io.github.pylonmc.pylon.core.block.MultiblockCache
import io.github.pylonmc.pylon.core.util.position.ChunkPosition
import org.bukkit.block.Block
import org.jetbrains.annotations.ApiStatus


/**
 * Multiblocks are more difficult than normal Pylon blocks for the simple reason that a multiblock
 * may contain some blocks that have not been loaded because they are in a different chunk.
 *
 * Ticking multiblocks should only tick when isFormedAndFullyLoaded() returns true, to avoid
 * ticking a multiblock that is either not formed, or not fully loaded (ie, not all of its components
 * are loaded).
 */
interface PylonMultiblock {
    // This is automatically implemented by PylonBlock (lol)
    val block: Block

    /**
     * All chunks containing at least one component of the multiblock
     */
    val chunksOccupied: Set<ChunkPosition>

    /**
     * Warning: You should not call this yourself. If you want to know if the multiblock is formed,
     * use isFormedAndFullyLoaded(), which will read from a cache, so is much faster.
     *
     * You can assume that when this method is called, all components of the multiblock are loaded.
     */
    @ApiStatus.Internal
    fun checkFormed(): Boolean

    /**
     * Returns true if every component of the multiblock is loaded AND the multiblock is formed.
     * Reads from a cache, so call as often as you want.
     */
    fun isFormedAndFullyLoaded(): Boolean
            = MultiblockCache.isFormed(this)

    /**
     * Should return true if there is any scenario in which this block's position could be part
     * of a formed multiblock.
     *
     * This could be called often, so make it lightweight.
     */
    fun isPartOfMultiblock(otherBlock: Block): Boolean
}