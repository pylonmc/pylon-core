package io.github.pylonmc.pylon.core.block.base

import org.bukkit.block.Block

interface Multiblock {
    val formed: Boolean

    /**
     * Called when one of the components is updated (added, broken, or modified)
     * Should check the multiblock is still valid and update any relevant state
     */
    fun refresh()

    /**
     * Returns true if and only if the block is a valid component of the multiblock
     */
    fun isComponent(block: Block): Boolean
}