package io.github.pylonmc.pylon.core.block

import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.block.context.BlockCreateContext
import io.github.pylonmc.pylon.core.block.context.BlockItemContext
import io.github.pylonmc.pylon.core.block.waila.WailaConfig
import io.github.pylonmc.pylon.core.config.Config
import io.github.pylonmc.pylon.core.config.Settings
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.event.PylonBlockDeserializeEvent
import io.github.pylonmc.pylon.core.event.PylonBlockSerializeEvent
import io.github.pylonmc.pylon.core.i18n.AddonTranslator
import io.github.pylonmc.pylon.core.item.PylonItemSchema
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.position.BlockPosition
import io.github.pylonmc.pylon.core.util.position.position
import io.github.pylonmc.pylon.core.util.pylonKey
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer

open class PylonBlock protected constructor(val block: Block) {

    val schema = PylonBlockSchema.schemaCache.remove(block.position)!!
    val key = schema.key

    @JvmSynthetic
    internal var errorBlock: BlockDisplay? = null

    open val name: Component = Component.translatable("pylon.${schema.key.namespace}.item.${schema.key.key}.waila", "pylon.${schema.key.namespace}.item.${schema.key.key}.name")

    constructor(block: Block, context: BlockCreateContext) : this(block)
    constructor(block: Block, pdc: PersistentDataContainer) : this(block)

    protected open fun postLoad() {}

    open fun getWaila(player: Player): WailaConfig {
        return WailaConfig(name)
    }

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

    fun getSettings(): Config
        = Settings.get(key)

    companion object {

        private val pylonBlockKeyKey = pylonKey("pylon_block_key")
        private val pylonBlockPositionKey = pylonKey("position")
        private val pylonBlockErrorKey = pylonKey("error")

        @JvmStatic
        fun register(key: NamespacedKey, material: Material, blockClass: Class<out PylonBlock>) {
            val schema = PylonBlockSchema(key, material, blockClass)
            PylonRegistry.BLOCKS.register(schema)
        }

        @JvmStatic
        inline fun <reified T: PylonBlock>register(key: NamespacedKey, material: Material)
            = register(key, material, T::class.java)

        @JvmSynthetic
        internal fun serialize(
            block: PylonBlock,
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
            PylonBlockSerializeEvent(block.block, block, pdc).callEvent()

            return pdc
        }

        @JvmSynthetic
        internal fun deserialize(
            world: World,
            pdc: PersistentDataContainer
        ): PylonBlock? {
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
                if (schema == null) {
                    PylonBlockSchema.schemaCache[position] = PhantomBlock.schema
                    return PhantomBlock(pdc, key, position.block)
                }

                // We can assume this function is only going to be called when the block's world is loaded, hence the asBlock!!
                @Suppress("UNCHECKED_CAST") // The cast will work - this is checked in the schema constructor
                val block = schema.load(position.block, pdc)

                block.errorBlock = pdc.get(pylonBlockErrorKey, PylonSerializers.UUID)
                    ?.let { world.getEntity(it) as? BlockDisplay }

                PylonBlockDeserializeEvent(block.block, block, pdc).callEvent()
                block.postLoad()
                return block
            } catch (t: Throwable) {
                PylonCore.logger.severe("Error while loading block $key at $position")
                t.printStackTrace()
                return if (key != null && position != null) {
                    PylonBlockSchema.schemaCache[position] = PhantomBlock.schema
                    PhantomBlock(pdc, key, position.block)
                } else {
                    null
                }
            }
        }
    }
}