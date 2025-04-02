@file:JvmSynthetic // Hide the ugly `ChunkPositionKt.getPosition` function in Java

package io.github.pylonmc.pylon.core.util.position

import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Block
import java.lang.ref.WeakReference

class ChunkPosition(world: World?, val x: Int, val z: Int) {
    private val worldRef: WeakReference<World> = WeakReference(world)
    val world: World?
        get() = worldRef.get()

    val asLong: Long
        get() = (x.toLong() shl 32) or (z.toLong() and 0xFFFFFFFFL)

    val chunk: Chunk?
        get() = world?.getChunkAt(x, z)

    /**
     * Obtaining an instance of a Chunk (eg through block.getChunk()) will
     * often LOAD THE CHUNK ITSELF. Yes, actually. This method does not
     * load the chunk, so it's a safe way to check if a chunk is loaded.
     */
    val loaded: Boolean
        get() = world?.isChunkLoaded(x, z) == true

    constructor(world: World, asLong: Long) : this(world, (asLong shr 32).toInt(), asLong.toInt())

    constructor(chunk: Chunk) : this(chunk.world, chunk.x, chunk.z)

    constructor(location: Location) : this(location.world, location.blockX shr 4, location.blockZ shr 4)

    constructor(block: Block) : this(block.location)

    override fun hashCode(): Int {
        val prime = 31
        return prime * (world?.hashCode() ?: 0) + prime * asLong.hashCode()
    }

    override fun toString(): String {
        return if (world != null) {
            "$x, $z in ${world!!.name}"
        } else {
            "$x, $z"
        }
    }

    override fun equals(other: Any?): Boolean {
        return (other is ChunkPosition)
                && other.world?.uid == world?.uid
                && other.x == x
                && other.z == z
    }
}

val Chunk.position: ChunkPosition
    get() = ChunkPosition(this)
