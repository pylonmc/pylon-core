package io.github.pylonmc.pylon.core.network

import io.github.pylonmc.pylon.core.block.BlockPosition
import io.github.pylonmc.pylon.core.block.position
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.block.BlockFace

abstract class Network(val origin: Location) {

    abstract val checkDistance: Int

    open val maxNetworkDepth: Int = Int.MAX_VALUE

    var root: NetworkTree = NetworkTree.Leaf(origin.block.position)

    abstract fun isValidBlock(block: Block): Boolean

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
            for (direction in ORTHOGONAL) {
                repeat(checkDistance) {
                    val relative = block.getRelative(direction, it)
                    if (isValidBlock(relative)) {
                        scanQueue.addLast(NextScan(relative, currentDepth + 1, currentNode))
                    }
                }
            }
        }

        root = rootTree.toImmutable()
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

private val ORTHOGONAL = arrayOf(
    BlockFace.NORTH,
    BlockFace.EAST,
    BlockFace.SOUTH,
    BlockFace.WEST,
    BlockFace.UP,
    BlockFace.DOWN
)