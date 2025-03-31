package io.github.pylonmc.pylon.core.block.base

import io.github.pylonmc.pylon.core.util.position.ChunkPosition
import org.bukkit.block.Block


/**
 * Multiblocks are more difficult than normal Pylon blocks for the simple reason that a multiblock
 * may contain some blocks that have not been loaded because they are in a different chunk. Due to
 * this, Multiblocks that implement Ticking will only be ticked when every chunk that the multiblock
 * spans has been loaded (referred to as 'fully loaded'). This is also the reason for chunksOccupied.
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