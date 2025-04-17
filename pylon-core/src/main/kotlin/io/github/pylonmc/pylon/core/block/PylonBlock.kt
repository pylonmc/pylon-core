package io.github.pylonmc.pylon.core.block

import io.github.pylonmc.pylon.core.block.context.BlockItemContext
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.persistence.blockstorage.PhantomBlock
import io.github.pylonmc.pylon.core.pluginInstance
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.position.BlockPosition
import io.github.pylonmc.pylon.core.util.position.position
import io.github.pylonmc.pylon.core.util.pylonKey
import net.kyori.adventure.text.Component
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.entity.BlockDisplay
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer

abstract class PylonBlock<out S : PylonBlockSchema> protected constructor(
    val schema: S,
    val block: Block
) {

    init {
        if (schema.key != PhantomBlock.key) {
            require(PylonRegistry.BLOCKS.contains(schema.key)) {
                "You can only create blocks using a registered schema; did you forget to register ${schema.key}?"
            }
        }
    }

    @JvmSynthetic
    internal var errorBlock: BlockDisplay? = null

    open fun getItem(context: BlockItemContext): ItemStack? {
        val defaultItem = PylonRegistry.ITEMS[schema.key]?.itemStack
        return when (context) {
            is BlockItemContext.Break -> if (context.context.normallyDrops) {
                defaultItem
            } else {
                null
            }

            is BlockItemContext.PickBlock -> defaultItem
        }
    }

    open fun write(pdc: PersistentDataContainer) {}

    companion object {

        private val pylonBlockKeyKey = pylonKey("key")
        private val pylonBlockPositionKey = pylonKey("position")
        private val pylonBlockErrorKey = pylonKey("error")

        @JvmSynthetic
        internal fun serialize(
            block: PylonBlock<*>,
            context: PersistentDataAdapterContext
        ): PersistentDataContainer {
            // See PhantomBlock docs for why we do this
            if (block is PhantomBlock) {
                return block.pdc
            }

            val pdc = context.newPersistentDataContainer()
            pdc.set(pylonBlockKeyKey, PylonSerializers.NAMESPACED_KEY, block.schema.key)
            pdc.set(pylonBlockPositionKey, PylonSerializers.LONG, block.block.position.asLong)

            val errorBlock = block.errorBlock
            if (errorBlock != null) {
                pdc.set(pylonBlockErrorKey, PylonSerializers.UUID, errorBlock.uniqueId)
            }

            block.write(pdc)
            return pdc
        }

        @JvmSynthetic
        internal fun deserialize(
            world: World,
            pdc: PersistentDataContainer
        ): PylonBlock<*>? {
            // Stored outside of the try block so they are displayed in error messages once acquired
            var key: NamespacedKey? = null
            var position: BlockPosition? = null

            try {
                key = pdc.get(pylonBlockKeyKey, PylonSerializers.NAMESPACED_KEY)
                    ?: error("Block PDC does not contain ID")

                position = pdc.get(pylonBlockPositionKey, PylonSerializers.LONG)?.let {
                    BlockPosition(world, it)
                } ?: error("Block PDC does not contain position")

                // We fail silently here because this may trigger if an addon is removed or fails to load.
                // In this case, we don't want to delete the data, and we also don't want to spam errors.
                // See PhantomBlock docs for why PhantomBlock is returned rather than null.
                val schema = PylonRegistry.BLOCKS[key]
                    ?: return PhantomBlock(pdc, key, position.block)

                // We can assume this function is only going to be called when the block's world is loaded, hence the asBlock!!
                @Suppress("UNCHECKED_CAST") // The cast will work - this is checked in the schema constructor
                val block = schema.loadConstructor.invoke(schema, position.block, pdc) as PylonBlock<*>

                block.errorBlock = pdc.get(pylonBlockErrorKey, PylonSerializers.UUID)
                    ?.let { world.getEntity(it) as? BlockDisplay }

                return block
            } catch (t: Throwable) {
                pluginInstance.logger.severe("Error while loading block $key at $position")
                t.printStackTrace()
                return if (key != null && position != null) {
                    PhantomBlock(pdc, key, position.block)
                } else {
                    null
                }
            }
        }
    }
}