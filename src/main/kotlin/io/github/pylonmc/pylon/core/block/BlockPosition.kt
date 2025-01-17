package io.github.pylonmc.pylon.core.block

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Block
import java.lang.ref.WeakReference

class BlockPosition(world: World, val x: Int, val y: Int, val z: Int) {
    private val worldRef: WeakReference<World> = WeakReference(world)
    val world: World?
        get() = worldRef.get()

    val chunk: ChunkPosition?
        get() = world?.let { ChunkPosition(it, x shr 4, z shr 4) }

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

    override fun equals(other: Any?): Boolean {
        if (other is BlockPosition && world != null && other.world != null) {
            return other.world!!.uid == world!!.uid && other.asLong == asLong
        }
        return false
    }
}

val Block.position: BlockPosition
    get() = BlockPosition(this)
