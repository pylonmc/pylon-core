package io.github.pylonmc.pylon.core.block

import io.github.pylonmc.pylon.core.persistence.PylonDataReader
import io.github.pylonmc.pylon.core.persistence.PylonDataWriter
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import org.bukkit.NamespacedKey
import org.bukkit.block.Block

abstract class PylonBlock<out S: PylonBlockSchema> protected constructor(
    val schema: S,
    val block: Block
) {

    constructor(reader: PylonDataReader, block: Block)
            : this(getSchemaOfType<S>(reader.id), block)

    fun write(writer: PylonDataWriter) {}

    // TODO listener
    fun tick() {}

    // TODO listener
    fun onRightClick() {}

    // TODO listener
    fun onBreak() {}

    companion object {
        /*
         * Convenience function to use in the (StateReader, Block) constructor
         */
        private fun <S : PylonBlockSchema> getSchemaOfType(key: NamespacedKey): S {
            val schema = PylonRegistry.BLOCKS.getOrThrow(key)

            // Dealing with deserialization, so not really any way around this
            @Suppress("UNCHECKED_CAST")
            return schema as S
        }
    }
}