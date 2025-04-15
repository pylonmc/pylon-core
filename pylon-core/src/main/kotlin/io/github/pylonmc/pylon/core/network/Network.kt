package io.github.pylonmc.pylon.core.network

import io.github.pylonmc.pylon.core.util.position.BlockPosition
import io.github.pylonmc.pylon.core.util.position.position
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.block.BlockFace

abstract class Network(val origin: Location) {

    abstract val checkDistance: Int

    open val maxNetworkDepth: Int = Int.MAX_VALUE

    var root: NetworkTree = NetworkTree.Leaf(origin.block.position)
    var allNodes: Set<NetworkTree> = setOf(root)
        private set

    var leaves: Set<NetworkTree.Leaf> = setOf(root as NetworkTree.Leaf)
        private set

    var branches: Set<NetworkTree.Branch> = emptySet()
        private set

    abstract fun isValidBlock(block: Block): Boolean
    abstract fun isLeaf(block: Block): Boolean

    open fun scan() {
        val rootTree = MutableNetworkTree(origin.block)
        val scanQueue = ArrayDeque(listOf(NextScan(origin.block, 0, rootTree)))
        val visited = mutableSetOf<BlockPosition>()
        while (scanQueue.isNotEmpty()) {
            val (block, currentDepth, parentNode) = scanQueue.removeFirst()
            if (currentDepth > maxNetworkDepth || !visited.add(block.position)) {
                continue
            }
            val currentNode = MutableNetworkTree(block)
            parentNode.children.add(currentNode)
            if (!isLeaf(block)) {
                for (direction in IMMEDIATE_FACES) {
                    repeat(checkDistance) {
                        val relative = block.getRelative(direction, it)
                        if (isValidBlock(relative)) {
                            scanQueue.addLast(NextScan(relative, currentDepth + 1, currentNode))
                        }
                    }
                }
            }
        }

        root = rootTree.toImmutable()
        allNodes = allNodes(root)
        leaves = allNodes.filterIsInstance<NetworkTree.Leaf>().toSet()
        branches = allNodes.filterIsInstance<NetworkTree.Branch>().toSet()
    }

    private fun allNodes(node: NetworkTree): Set<NetworkTree> {
        return when (node) {
            is NetworkTree.Leaf -> setOf(node)
            is NetworkTree.Branch -> node.children.flatMap { allNodes(it) }.toSet()
        }
    }

    private data class NextScan(val block: Block, val depth: Int, val parent: MutableNetworkTree)

    private data class MutableNetworkTree(
        val block: Block,
        val children: MutableSet<MutableNetworkTree> = mutableSetOf()
    ) {
        fun toImmutable(): NetworkTree {
            return if (children.isEmpty()) {
                NetworkTree.Leaf(block.position)
            } else {
                NetworkTree.Branch(block.position, children.map { it.toImmutable() }.toSet())
            }
        }
    }

    companion object {

        @JvmField
        public val IMMEDIATE_FACES: Array<BlockFace> = arrayOf<BlockFace>(
            BlockFace.UP,
            BlockFace.DOWN,
            BlockFace.EAST,
            BlockFace.WEST,
            BlockFace.SOUTH,
            BlockFace.NORTH
        )

        @JvmField
        public val IMMEDIATE_FACES_WITH_DIAGONALS: Array<BlockFace> = arrayOf<BlockFace>(
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