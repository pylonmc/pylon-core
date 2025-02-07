package io.github.pylonmc.pylon.core.block

import io.github.pylonmc.pylon.core.persistence.blockstorage.PhantomBlock
import io.github.pylonmc.pylon.core.persistence.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.pluginInstance
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer

abstract class PylonBlock<out S: PylonBlockSchema> protected constructor(
    val schema: S,
    val block: Block
) {
    open fun write(pdc: PersistentDataContainer) {}

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

        private val pylonBlockIdKey = NamespacedKey(pluginInstance, "id")
        private val pylonBlockPositionKey = NamespacedKey(pluginInstance, "position")

        internal fun serialize(
            block: PylonBlock<PylonBlockSchema>,
            context: PersistentDataAdapterContext
        ): PersistentDataContainer {
            // See PhantomBlock docs for why we do this
            if (block is PhantomBlock) {
                return block.pdc
            }

            val pdc = context.newPersistentDataContainer()
            pdc.set(pylonBlockIdKey, PylonSerializers.NAMESPACED_KEY, block.schema.key)
            pdc.set(pylonBlockPositionKey, PylonSerializers.LONG, block.block.position.asLong)
            block.write(pdc)
            return pdc
        }

        internal fun deserialize(
            world: World,
            pdc: PersistentDataContainer
        ): PylonBlock<PylonBlockSchema>? {
            // Stored outside of the try block so they are displayed in error messages once acquired
            var id: NamespacedKey? = null
            var position: BlockPosition? = null

            try {
                id = pdc.get(pylonBlockIdKey, PylonSerializers.NAMESPACED_KEY)
                    ?: error("Block PDC does not contain ID")

                position = pdc.get(pylonBlockPositionKey, PylonSerializers.LONG)?.let {
                    BlockPosition(world, it)
                } ?: error("Block PDC does not contain position")

                // We fail silently here because this may trigger if an addon is removed or fails to load
                // In this case, we don't want to delete the data, and we also don't want to spam errors
                // See PhantomBlock docs for why PhantomBlock is returned rather than null
                val schema = PylonRegistry.BLOCKS[id]
                    ?: return PhantomBlock(pdc, position.block)

                // We can assume this function is only going to be called when the block's world is loaded, hence the asBlock!!
                @Suppress("UNCHECKED_CAST") // The cast will work - this is checked in the schema constructor
                return schema.loadConstructor.invoke(schema, pdc, position.block) as PylonBlock<PylonBlockSchema>

            } catch (t: Throwable) {
                pluginInstance.logger.severe("Error while loading block $id at $position")
                t.printStackTrace()
                return if (position != null) {
                    PhantomBlock(pdc, position.block)
                } else {
                    null
                }
            }
        }
    }
}