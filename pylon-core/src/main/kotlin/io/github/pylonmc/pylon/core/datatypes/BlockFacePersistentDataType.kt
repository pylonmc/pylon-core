package io.github.pylonmc.pylon.core.datatypes

import org.bukkit.block.BlockFace
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType

object BlockFacePersistentDataType : PersistentDataType<String, BlockFace> {

    override fun getPrimitiveType(): Class<String> = String::class.java

    override fun getComplexType(): Class<BlockFace> = BlockFace::class.java

    override fun fromPrimitive(primitive: String, context: PersistentDataAdapterContext): BlockFace
        = BlockFace.valueOf(primitive)

    override fun toPrimitive(complex: BlockFace, context: PersistentDataAdapterContext): String
        = complex.name
}