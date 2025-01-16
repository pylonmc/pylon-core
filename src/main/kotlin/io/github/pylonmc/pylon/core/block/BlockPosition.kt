package io.github.pylonmc.pylon.core.block

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Block
import java.lang.ref.WeakReference

class BlockPosition(world: World, x: Int, y: Int, z: Int) {
    private val world: WeakReference<World> = WeakReference(world)
    private var asLong: Long = x.and(0x3FFFFFF).toLong().shl(38)
        .or(z.and(0x3FFFFFF).toLong().shl(12))
        .or(y.and(0xFFF).toLong())

    constructor(location: Location) : this(location.world, location.blockX, location.blockY, location.blockZ)

    constructor(block: Block) : this(block.world, block.x, block.y, block.z)

    val x: Int = asLong.shr(38).toInt()
    val y: Int = asLong.shl(52).shr(52).toInt()
    val z: Int = asLong.shl(26).shr(38).toInt()

    val chunkX = x.shr(4)
    val chunkZ = z.shr(4)

    override fun hashCode(): Int {
        val prime = 31
        val ref = world.get()
        return prime * (ref?.hashCode() ?: 0) + prime * asLong.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other is BlockPosition) {
            if (world.get() != null && other.world.get() != null) {
                return other.world.get()!!.uid == world.get()!!.uid && other.asLong == asLong
            }
        }
        return false
    }
}