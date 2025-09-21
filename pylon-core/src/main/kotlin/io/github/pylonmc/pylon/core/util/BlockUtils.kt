@file:JvmName("BlockUtils")

package io.github.pylonmc.pylon.core.util

import org.bukkit.block.Block
import org.bukkit.block.BlockFace

/**
 * [BlockFace.UP], [BlockFace.DOWN], [BlockFace.EAST], [BlockFace.WEST], [BlockFace.SOUTH], [BlockFace.NORTH]
 */
@JvmField
val IMMEDIATE_FACES: Array<BlockFace> = arrayOf(
    BlockFace.UP,
    BlockFace.DOWN,
    BlockFace.EAST,
    BlockFace.WEST,
    BlockFace.SOUTH,
    BlockFace.NORTH
)

/**
 * Same as [IMMEDIATE_FACES] but includes diagonal faces, not including the vertical directions.
 */
@JvmField
val IMMEDIATE_FACES_WITH_DIAGONALS: Array<BlockFace> = arrayOf(
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

val Block.replaceableOrAir: Boolean
    get() = type.isAir || isReplaceable