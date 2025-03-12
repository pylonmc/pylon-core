package io.github.pylonmc.pylon.core.network

import io.github.pylonmc.pylon.core.util.position.BlockPosition

sealed interface NetworkTree {

    val position: BlockPosition

    data class Branch(override val position: BlockPosition, val children: Set<NetworkTree>) : NetworkTree

    data class Leaf(override val position: BlockPosition) : NetworkTree
}