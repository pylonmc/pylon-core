package io.github.pylonmc.pylon.core.util.position

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Block
import org.joml.Vector3i
import java.lang.ref.WeakReference

/**
 * Represents the position of a block (x, y, z, and world).
 *
 * Why not just use [Block]? Because [Block] contains lots of extra information such
 * as the type of the block, and so cannot practically be serialized. Holding
 * references to blocks for extended periods may also prevent chunks from unloading,
 * and increase memory usage.
 */
class BlockPosition(world: World?, val x: Int, val y: Int, val z: Int) {
    private val worldRef: WeakReference<World> = WeakReference(world)
    val world: World?
        get() = worldRef.get()

    val chunk: ChunkPosition
        get() = ChunkPosition(world, x shr 4, z shr 4)

    @get:JvmSynthetic
    internal val asLong: Long
        get() = ((x and 0x3FFFFFF).toLong() shl 38)
            .or((z and 0x3FFFFFF).toLong() shl 12)
            .or((y and 0xFFF).toLong())

    internal constructor(world: World, asLong: Long) : this(
        world,
        (asLong shr 38).toInt(),
        ((asLong shl 52) shr 52).toInt(),
        ((asLong shl 26) shr 38).toInt()
    )

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

    fun addScalar(x: Int, y: Int, z: Int): BlockPosition {
        return BlockPosition(world, this.x + x, this.y + y, this.z + z)
    }

    fun withScalar(x: Int, y: Int, z: Int): BlockPosition {
        return BlockPosition(world, x, y, z)
    }

    operator fun plus(other: BlockPosition): BlockPosition {
        check(world == other.world) { "Cannot add two BlockPositions in different worlds" }
        return BlockPosition(world, x + other.x, y + other.y, z + other.z)
    }

    operator fun plus(other: Vector3i): BlockPosition {
        return BlockPosition(world, x + other.x, y + other.y, z + other.z)
    }

    operator fun minus(other: BlockPosition): BlockPosition {
        check(world == other.world) { "Cannot subtract two BlockPositions in different worlds" }
        return BlockPosition(world, x - other.x, y - other.y, z - other.z)
    }

    operator fun minus(other: Vector3i): BlockPosition {
        return BlockPosition(world, x + other.x, y + other.y, z + other.z)
    }

    operator fun times(value: Int): BlockPosition {
        return BlockPosition(world, x * value, y * value, z * value)
    }

    operator fun div(value: Int): BlockPosition {
        return BlockPosition(world, x / value, y / value, z / value)
    }

    val vector3i: Vector3i
        get() = Vector3i(x, y, z)

    val location: Location
        get() = Location(world, x.toDouble(), y.toDouble(), z.toDouble())

    val block: Block
        get() = world?.getBlockAt(x, y, z) ?: error("World is null")
}

@get:JvmSynthetic
val Block.position: BlockPosition
    get() = BlockPosition(this)
