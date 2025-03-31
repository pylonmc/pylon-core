package io.github.pylonmc.pylon.core.block.base

import io.github.pylonmc.pylon.core.block.MultiblockCache
import io.github.pylonmc.pylon.core.util.position.ChunkPosition
import org.bukkit.block.Block


/**
 * Multiblocks are more difficult than normal Pylon blocks for the simple reason that a multiblock
 * may contain some blocks that have not been loaded because they are in a different chunk.
 *
 * Ticking multiblocks should only tick when isFormedAndFullyLoaded() returns true, to avoid ticking
 * a multiblock that is either not formed, or not fully loaded (ie, not all of its components are
 * loaded).
 */
interface Multiblock {
    var formed: Boolean

    // This is automatically implemented by PylonBlock (lol)
    val block: Block

    /**
     * All chunks containing at least one component of the multiblock
     */
    val chunksOccupied: Set<ChunkPosition>

    /**
     * Check whether the multiblock is formed, and update the formed variable accordingly.
     *
     * You can assume that when this method is called, all components of the multiblock are loaded.
     */
    fun refresh()

    /**
     * Should return true if there is any scenario in which this block could be part of a formed
     * multiblock.
     *
     * This could be called often, so make it lightweight.
     */
    fun isPartOfMultiblock(otherBlock: Block): Boolean

    /**
     * Convenience method. You should use this to check if the multiblock can be ticked.
     */
    fun isFormedAndFullyLoaded(): Boolean
        = formed && isFullyLoaded()

    /**
     * Returns true if all the chunks that this multiblock can occupy are loaded.
     */
    fun isFullyLoaded(): Boolean
        = MultiblockCache.isFullyLoaded(this)

    /**
     * Called when a block in any of the multiblock's chunks is modified (eg: water turning into ice).
     *
     * You can assume that when this method is called, all components of the multiblock are loaded.
     *
     * You cannot assume that the change has been made yet. For example, if ice melts into water, the
     * supplied otherBlock will be of type water, but the block in-world may still be of type ice.
     *
     * You can that any BlockStorage updates have already taken place.
     */
    fun onComponentModified(newBlock: Block)
}