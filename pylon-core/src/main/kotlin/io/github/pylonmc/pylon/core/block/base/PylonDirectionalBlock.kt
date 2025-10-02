package io.github.pylonmc.pylon.core.block.base

import io.github.pylonmc.pylon.core.block.PylonBlock
import org.bukkit.block.BlockFace

/**
 * Represents a block that has a specific facing direction.
 *
 * Internally only used for rotating [PylonBlock.blockTextureEntity]s.
 */
interface PylonDirectionalBlock {
    fun getFacing(): BlockFace?
}