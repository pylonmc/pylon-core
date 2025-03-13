@file:JvmSynthetic // Hide the ugly `BlockPositionKt.getPosition` function in Java

package io.github.pylonmc.pylon.core.util

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Block
import java.lang.ref.WeakReference

open class BlockPosition(world: World?, val x: Int, val y: Int, val z: Int) {
    private val worldRef: WeakReference<World> = WeakReference(world)
    val world: World?
        get() = worldRef.get()

    val chunk: ChunkPosition
        get() = ChunkPosition(world, x shr 4, z shr 4)

    val asLong: Long
        get() = ((x and 0x3FFFFFF).toLong() shl 38)
            .or((z and 0x3FFFFFF).toLong() shl 12)
            .or((y and 0xFFF).toLong())

    constructor(world: World, asLong: Long) : this(world,
        (asLong shr 38).toInt(),
        ((asLong shl 52) shr 52).toInt(),
        ((asLong shl 26) shr 38).toInt())

    constructor(location: Location) : this(location.world, location.blockX, location.blockY, location.blockZ)

    constructor(block: Block) : this(block.world, block.x, block.y, block.z)

    override fun hashCode(): Int {
        val prime = 31
        return prime * (world?.hashCode() ?: 0) + prime * asLong.hashCode()
    }

    override fun toString(): String {
        return if (world != null) {
            "$x, $y, $z in ${world!!.name}"
        } else {
            "$x, $y, $z"
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other is BlockPosition) {
            return other.world?.uid == world?.uid && other.asLong == asLong
        }
        return false
    }

    operator fun plus(other: BlockPosition): BlockPosition {
        check(world == other.world) { "Cannot add two BlockPositions in different worlds" }
        return BlockPosition(world, x + other.x, y + other.y, z + other.z)
    }

    operator fun minus(other: BlockPosition): BlockPosition {
        check(world == other.world) { "Cannot subtract two BlockPositions in different worlds" }
        return BlockPosition(world, x - other.x, y - other.y, z - other.z)
    }

    operator fun times(value: Int): BlockPosition {
        return BlockPosition(world, x * value, y * value, z * value)
    }

    operator fun div(value: Int): BlockPosition {
        return BlockPosition(world, x / value, y / value, z / value)
    }

    val location: Location
        get() = Location(world, x.toDouble(), y.toDouble(), z.toDouble())

    val block: Block
        get() = world?.getBlockAt(x, y, z) ?: error("World is null")
}

val Block.position: BlockPosition
    get() = BlockPosition(this)
