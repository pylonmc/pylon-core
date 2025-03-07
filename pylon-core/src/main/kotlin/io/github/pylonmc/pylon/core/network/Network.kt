package io.github.pylonmc.pylon.core.network

import io.github.pylonmc.pylon.core.util.BlockPosition
import io.github.pylonmc.pylon.core.util.position
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

    protected open val scanDirections: Array<BlockFace> = arrayOf(
        BlockFace.NORTH,
        BlockFace.EAST,
        BlockFace.SOUTH,
        BlockFace.WEST,
        BlockFace.UP,
        BlockFace.DOWN
    )

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
                for (direction in scanDirections) {
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
}