package io.github.pylonmc.pylon.core.block

import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.block.PylonBlock.Companion.register
import io.github.pylonmc.pylon.core.block.base.PylonEntityHolderBlock
import io.github.pylonmc.pylon.core.block.base.PylonGuiBlock
import io.github.pylonmc.pylon.core.block.context.BlockBreakContext
import io.github.pylonmc.pylon.core.block.context.BlockCreateContext
import io.github.pylonmc.pylon.core.block.context.BlockItemContext
import io.github.pylonmc.pylon.core.block.waila.WailaConfig
import io.github.pylonmc.pylon.core.config.Config
import io.github.pylonmc.pylon.core.config.Settings
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.event.PylonBlockDeserializeEvent
import io.github.pylonmc.pylon.core.event.PylonBlockLoadEvent
import io.github.pylonmc.pylon.core.event.PylonBlockSerializeEvent
import io.github.pylonmc.pylon.core.event.PylonBlockUnloadEvent
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.position.BlockPosition
import io.github.pylonmc.pylon.core.util.position.position
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer

/**
 * All custom Pylon blocks extend this class. Every instance of this class is wrapping a real block
 * in the world, and is stored in [BlockStorage]. All new block *types* must be registered using [register].
 *
 * An implementation of PylonBlock must have two constructors: one that takes a [Block] and a
 * [BlockCreateContext], and one that takes a [Block] and a [PersistentDataContainer]. The first
 * constructor is known as the "create constructor", and is used when the block is created in the world.
 * The second constructor is known as the "load constructor", and is used to reconstruct the block when
 * the chunk containing the block is loaded.
 */
open class PylonBlock protected constructor(val block: Block) {

    /**
     * The schema of a block is all the data needed to create or load the block.
     */
    val schema = PylonBlockSchema.schemaCache.remove(block.position)!!
    val key = schema.key

    val defaultTranslationKey = schema.defaultBlockTranslationKey

    /**
     * This constructor is called when a *new* block is created in the world
     * ex:
     * - A player places a block
     * - `BlockStorage.placeBlock` called
     *
     * @see PylonBlockSchema.create
     * @see PylonBlockUnloadEvent
     */
    constructor(block: Block, context: BlockCreateContext) : this(block)

    /**
     * This constructor is called while the chunk is being loaded
     *
     * @see PylonBlockSchema.load
     * @see PylonBlockLoadEvent
     * @see deserialize
     */
    constructor(block: Block, pdc: PersistentDataContainer) : this(block)

    /**
     * Since "external" stuff like [PylonGuiBlock] and [PylonEntityHolderBlock] load their
     * data *after* the load constructor is called, this method is necessary to manipulate
     * the data loaded by those interfaces
     */
    protected open fun postLoad() {}

    /**
     * This will only be called for the player if the player has WAILA enabled
     *
     * @return the WAILA configuration, or null if WAILA should not be shown for this block
     */
    open fun getWaila(player: Player): WailaConfig? {
        return WailaConfig(defaultTranslationKey)
    }

    /**
     * Called when the corresponding item of the block is requested. By default,
     * returns the item with the same key as the block. If the block is
     * being broken, the item will only be returned if [BlockBreakContext.normallyDrops]
     * is true, otherwise it will return null.
     *
     * @return the item, or null if none
     */
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

    /**
     * Called when the block is saved
     *
     * @see serialize
     */
    open fun write(pdc: PersistentDataContainer) {}

    fun getSettings(): Config = Settings.get(key)

    companion object {

        private val pylonBlockKeyKey = pylonKey("pylon_block_key")
        private val pylonBlockPositionKey = pylonKey("position")

        @JvmStatic
        fun register(key: NamespacedKey, material: Material, blockClass: Class<out PylonBlock>, placementPredicate: (BlockPosition, PylonBlockSchema) -> Boolean) {
            val schema = PylonBlockSchema(key, material, blockClass, placementPredicate)
            PylonRegistry.BLOCKS.register(schema)
        }

        @JvmStatic
        fun register(key: NamespacedKey, material: Material, blockClass: Class<out PylonBlock>) =
            register(key, material, blockClass, {_, _ -> true})

        @JvmSynthetic
        inline fun <reified T : PylonBlock> register(key: NamespacedKey, material: Material) =
            register(key, material, T::class.java, {_,_ -> true})


        @JvmSynthetic
        inline fun <reified T: PylonBlock> register(key: NamespacedKey, material: Material, noinline placementPredicate: (BlockPosition, PylonBlockSchema) -> Boolean) =
            register(key, material, T::class.java, placementPredicate)

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