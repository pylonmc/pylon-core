package io.github.pylonmc.pylon.core.block.base

import org.bukkit.block.BlockFace

/**
 * Represents a block that has a specific facing direction.
 */
interface PylonDirectionalBlock {
    fun getFacing(): BlockFace
}