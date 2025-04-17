package io.github.pylonmc.pylon.core.util

import org.bukkit.block.BlockFace

open class BlockUtils {
    companion object {
        @JvmField
        val IMMEDIATE_FACES: Array<BlockFace> = arrayOf<BlockFace>(
            BlockFace.UP,
            BlockFace.DOWN,
            BlockFace.EAST,
            BlockFace.WEST,
            BlockFace.SOUTH,
            BlockFace.NORTH
        )

        @JvmField
        val IMMEDIATE_FACES_WITH_DIAGONALS: Array<BlockFace> = arrayOf<BlockFace>(
            BlockFace.UP,
            BlockFace.DOWN,
            BlockFace.EAST,
            BlockFace.WEST,
            BlockFace.SOUTH,
            BlockFace.NORTH,
            BlockFace.NORTH_EAST,
            BlockFace.NORTH_WEST,
            BlockFace.SOUTH_EAST,
            BlockFace.SOUTH_WEST,
            BlockFace.EAST
        )
    }
}