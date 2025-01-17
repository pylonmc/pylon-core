package io.github.pylonmc.pylon.core.block

import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Block
import java.lang.ref.WeakReference

class ChunkPosition(world: World, val x: Int, val z: Int) {
    private val worldRef: WeakReference<World> = WeakReference(world)
    val world: World?
        get() = worldRef.get()

    val asLong = (x.toLong() shl 32) or (z.toLong() and 0xFFFFFFFFL)

    constructor(world: World, asLong: Long) : this(world, (asLong shr 32).toInt(), asLong.toInt())

    constructor(chunk: Chunk) : this(chunk.world, chunk.x, chunk.z)

    constructor(location: Location) : this(location.chunk)

    constructor(block: Block) : this(block.chunk)

    override fun hashCode(): Int {
        val prime = 31
        return prime * (world?.hashCode() ?: 0) + prime * asLong.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return (other is ChunkPosition)
                && other.x == x
                && other.z == z
    }
}

val Chunk.position: ChunkPosition
    get() = ChunkPosition(this)
