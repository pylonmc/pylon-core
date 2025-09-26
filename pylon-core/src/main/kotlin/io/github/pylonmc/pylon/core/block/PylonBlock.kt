package io.github.pylonmc.pylon.core.block

import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.block.PylonBlock.Companion.register
import io.github.pylonmc.pylon.core.block.base.PylonEntityHolderBlock
import io.github.pylonmc.pylon.core.block.base.PylonGuiBlock
import io.github.pylonmc.pylon.core.block.context.BlockBreakContext
import io.github.pylonmc.pylon.core.block.context.BlockCreateContext
import io.github.pylonmc.pylon.core.block.waila.WailaConfig
import io.github.pylonmc.pylon.core.config.Config
import io.github.pylonmc.pylon.core.config.Settings
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.event.PylonBlockDeserializeEvent
import io.github.pylonmc.pylon.core.event.PylonBlockSerializeEvent
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
 * Represents a Pylon block in the world.
 *
 * All custom Pylon blocks extend this class. Every instance of this class is wrapping a real block
 * in the world, and is stored in [BlockStorage]. All new block *types* must be registered using [register].
 *
 * An implementation of PylonBlock must have two constructors: one that takes a [Block] and a
 * [BlockCreateContext], and one that takes a [Block] and a [PersistentDataContainer]. The first
 * constructor is known as the "create constructor", and is used when the block is created in the world.
 * The second constructor is known as the "load constructor", and is used to reconstruct the block when
 * the chunk containing the block is loaded.
 *
 * @see BlockStorage
 */
open class PylonBlock internal constructor(val block: Block) {

    /**
     * All the data needed to create or load the block.
     */
    val schema = PylonBlockSchema.schemaCache.remove(block.position)!!

    val key = schema.key

    val defaultWailaTranslationKey = schema.defaultWailaTranslationKey

    /**
     * This constructor is called when a *new* block is created in the world.
     */
    constructor(block: Block, context: BlockCreateContext) : this(block)

    /**
     * This constructor is called when the block is loaded. For example, if the server
     * restarts, we need to create a new PylonBlock instance, and we'll do it with this
     * constructor.
     *
     * You should only load data in this constructor. If you need to do any extra logic on
     * load for whatever reason, it's recommended to do it in [postLoad] to make sure all
     * data associated with your block that you don't directly control (such as inventories,
     * associated entities, fluid tank data, etc) has been loaded.
     *
     * @see PersistentDataContainer
     */
    constructor(block: Block, pdc: PersistentDataContainer) : this(block)

    /**
     * Called after the load constructor.
     *
     * This is necessary because "external" stuff like [PylonGuiBlock], [io.github.pylonmc.pylon.core.block.base.PylonFluidBufferBlock]
     * and [PylonEntityHolderBlock] load their data *after* the load constructor is called.
     * If you need to use data from these interfaces (such as the amount of fluid stored in
     * a [io.github.pylonmc.pylon.core.block.base.PylonFluidBufferBlock], you must use this
     * instead of using the data in the load constructor.
     */
    protected open fun postLoad() {}

    /**
     * WAILA is the text that shows up when looking at a block to tell you what the block is.
     *
     * This will only be called for the player if the player has WAILA enabled.
     *
     * @return the WAILA configuration, or null if WAILA should not be shown for this block.
     */
    open fun getWaila(player: Player): WailaConfig? {
        return WailaConfig(defaultWailaTranslationKey)
    }

    /**
     * Returns the item that the block should drop.
     *
     * By default, returns the item with the same key as the block only if
     * [BlockBreakContext.normallyDrops] is true, and null otherwise.
     *
     * @return the item the block should drop, or null if none
     */
    open fun getDropItem(context: BlockBreakContext): ItemStack? {
        return if (context.normallyDrops) {
            PylonRegistry.ITEMS[schema.key]?.itemStack
        } else {
            null
        }
    }

    /**
     * Returns the item that should be given when the block is middle clicked.
     *
     * By default, returns the item with the same key as the block only if
     * [BlockBreakContext.normallyDrops] is true, and null otherwise.
     *
     * @return the item the block should give when middle clicked, or null if none
     */
    open fun getPickItem() = PylonRegistry.ITEMS[schema.key]?.itemStack

    /**
     * Called when the block is saved.
     *
     * Put any logic to save the data in the block here.
     *
     * *Do not assume that when this is called, the block is being unloaded.* This
     * may be called for other reasons, such as when a player right clicks with
     * [io.github.pylonmc.pylon.core.content.debug.DebugWaxedWeatheredCutCopperStairs].
     * Instead, implement [io.github.pylonmc.pylon.core.block.base.PylonUnloadBlock] and
     * use [io.github.pylonmc.pylon.core.block.base.PylonUnloadBlock.onUnload].
     */
    open fun write(pdc: PersistentDataContainer) {}

    /**
     * Returns settings associated with the block.
     *
     * Shorthand for `Settings.get(getKey())`
     */
    fun getSettings(): Config = Settings.get(key)

    companion object {

        private val pylonBlockKeyKey = pylonKey("pylon_block_key")
        private val pylonBlockPositionKey = pylonKey("position")

        @get:JvmStatic
        val Block.pylonBlock: PylonBlock?
            get() = BlockStorage.get(this)

        @get:JvmStatic
        val Block.isVanillaBlock: Boolean
            get() = BlockStorage.get(this) == null

        /**
         * Registers a new block type with Pylon.
         *
         * @param key A unique key that identifies this type of block
         * @param material The material to use as the block. This must match the material
         * of the item(s) that place the block.
         * @param blockClass The class extending [PylonBlock] that represents a block
         * of this type in the world.
         */
        @JvmStatic
        fun register(key: NamespacedKey, material: Material, blockClass: Class<out PylonBlock>) {
            val schema = PylonBlockSchema(key, material, blockClass)
            PylonRegistry.BLOCKS.register(schema)
        }

        @JvmSynthetic
        inline fun <reified T : PylonBlock> register(key: NamespacedKey, material: Material) =
            register(key, material, T::class.java)

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