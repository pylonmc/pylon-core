package io.github.pylonmc.pylon.core.block

import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Block
import java.lang.ref.WeakReference

class ChunkPosition(world: World, x: Int, z: Int) {
    val world: WeakReference<World> = WeakReference(world)
    private val asLong = (x.toLong() shl 32) or (z.toLong() and 0xFFFFFFFFL)
    val x = (asLong shr 32).toInt()
    val y = asLong.toInt()

    constructor(chunk: Chunk) : this(chunk.world, chunk.x, chunk.z)

    constructor(location: Location) : this(location.chunk)

    constructor(block: Block) : this(block.chunk)

    override fun hashCode(): Int {
        return asLong.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return (other is ChunkPosition) && other.asLong == asLong
    }
}
