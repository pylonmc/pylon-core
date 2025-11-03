package io.github.pylonmc.pylon.core.block

import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes
import com.github.retrooper.packetevents.protocol.world.Location
import com.github.retrooper.packetevents.util.Vector3f
import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.block.PylonBlock.Companion.register
import io.github.pylonmc.pylon.core.block.base.PylonDirectionalBlock
import io.github.pylonmc.pylon.core.block.base.PylonEntityHolderBlock
import io.github.pylonmc.pylon.core.block.base.PylonGuiBlock
import io.github.pylonmc.pylon.core.block.context.BlockBreakContext
import io.github.pylonmc.pylon.core.block.context.BlockCreateContext
import io.github.pylonmc.pylon.core.config.Config
import io.github.pylonmc.pylon.core.config.PylonConfig
import io.github.pylonmc.pylon.core.config.Settings
import io.github.pylonmc.pylon.core.content.debug.DebugWaxedWeatheredCutCopperStairs
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.event.PylonBlockDeserializeEvent
import io.github.pylonmc.pylon.core.event.PylonBlockSerializeEvent
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.position.BlockPosition
import io.github.pylonmc.pylon.core.util.position.position
import io.github.pylonmc.pylon.core.util.pylonKey
import io.github.pylonmc.pylon.core.waila.WailaDisplay
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import io.papermc.paper.datacomponent.DataComponentTypes
import me.tofaa.entitylib.meta.display.ItemDisplayMeta
import me.tofaa.entitylib.wrapper.WrapperEntity
import org.bukkit.Axis
import net.kyori.adventure.key.Key
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Directional
import org.bukkit.block.data.Orientable
import org.bukkit.block.data.Rotatable
import org.bukkit.entity.ItemDisplay
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

    val nameTranslationKey = schema.nameTranslationKey
    val loreTranslationKey = schema.loreTranslationKey
    val defaultWailaTranslationKey = schema.defaultWailaTranslationKey

    /**
     * Set this to `true` if your block should not have a [blockTextureEntity] for custom models/textures.
     *
     * For example, if your block is comprised fully of [ItemDisplay]s, then you may have no need for a texture
     * entity as your existing entities could already support custom models/textures.
     */
    open var disableBlockTextureEntity = false

    /**
     * A packet based [ItemDisplay] sent to players with `customBlockTextures` enabled.
     *
     * Being lazily initialized, if you do not access the entity directly it will only be created
     * when a player with `customBlockTextures` comes within range for the first time. This is to
     * avoid unnecessary entity creation, memory usage, and entity update overhead when no players
     * can actually see it.
     *
     * Upon initialization the entity is set up by [setupBlockTexture] (which can be overridden),
     * and modifications afterward can be done using [updateBlockTexture].
     *
     * For example, if you have a block that faces different directions, you can override [setupBlockTexture]
     * and rotate the entity based on the block's facing direction.
     *
     * Or let's say you have a furnace block that changes texture based on whether it's lit or not,
     * you can use [updateBlockTexture] to change the entity's item to reflect the lit/unlit state.
     */
    val blockTextureEntity: WrapperEntity? by lazy {
        if (!PylonConfig.BlockTextureConfig.enabled || disableBlockTextureEntity) {
            null
        } else {
            val entity = WrapperEntity(EntityTypes.ITEM_DISPLAY)
            val meta = entity.getEntityMeta(ItemDisplayMeta::class.java)
            setupBlockTexture(entity, meta)
        }
    }

    val defaultItem = PylonRegistry.ITEMS[schema.key]

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
     * Used to initialize [blockTextureEntity], if you need to modify the entity post-initialization,
     * use [updateBlockTexture].
     *
     * By default, this method sets the item display to be at the center of the block, using the
     * item returned by [getBlockTextureItem] (or a barrier if none is provided), set's its item
     * model to air, making it invisible for players without a resource pack, scales it to
     * 1.00085f in all directions to prevent z-fighting with the vanilla block model, and maxes its
     * brightness. If the block is directional (either by implementing [PylonDirectionalBlock],
     * or by having block data that is [Orientable], [Directional], or [Rotatable]), the entity
     * is rotated to face the same direction as the block.
     */
    protected open fun setupBlockTexture(entity: WrapperEntity, meta: ItemDisplayMeta): WrapperEntity = entity.apply {
        // TODO: Add a way to easily just change the transformation of the entity, without having to override this method entirely
        entity.spawn(Location(block.x + 0.5, block.y + 0.5, block.z + 0.5, 0f, 0f))

        val item = getBlockTextureItem() ?: ItemStack(Material.BARRIER)
        item.setData(DataComponentTypes.ITEM_MODEL, Key.key("air"))
        meta.item = SpigotConversionUtil.fromBukkitItemStack(item)
        meta.brightnessOverride = 15 shl 4 or 15 shl 20;
        meta.scale = Vector3f(1.0009f, 1.0009f, 1.0009f)
        meta.width = 0f
        meta.height = 0f

        val blockData = block.blockData
        var facing = (this@PylonBlock as? PylonDirectionalBlock)?.getFacing()
        if (facing == null) {
            if (blockData is Orientable) {
                facing = when (blockData.axis) {
                    Axis.X -> BlockFace.EAST
                    Axis.Y -> BlockFace.UP
                    Axis.Z -> BlockFace.SOUTH
                }
            } else if (blockData is Directional) {
                facing = blockData.facing
            } else if (blockData is Rotatable) {
                facing = blockData.rotation
            }
        }

        if (facing != null) {
            val direction = facing.direction
            entity.teleport(entity.location.apply {
                this.direction = Vector3f(direction.x.toFloat(), direction.y.toFloat(), direction.z.toFloat())
            })
        }
    }

    /**
     * Use this method to make any changes to the block texture entity, such as changing its item,
     * transformation, etc, after initialization. (see [setupBlockTexture])
     */
    protected fun updateBlockTexture(updater: (WrapperEntity, ItemDisplayMeta) -> Unit) {
        blockTextureEntity?.let {
            val meta = it.getEntityMeta(ItemDisplayMeta::class.java)
            updater(it, meta)
        }
    }

    /**
     * Call this method to refresh the block texture entity's item to be the result of
     * [getBlockTextureItem], or a barrier if that returns null.
     */
    protected fun refreshBlockTextureItem() {
        updateBlockTexture { _, meta ->
            val item = getBlockTextureItem() ?: ItemStack(Material.BARRIER)
            item.setData(DataComponentTypes.ITEM_MODEL, Key.key("air"))
            meta.item = SpigotConversionUtil.fromBukkitItemStack(item)
        }
    }

    /**
     * WAILA is the text that shows up when looking at a block to tell you what the block is.
     *
     * This will only be called for the player if the player has WAILA enabled.
     *
     * @return the WAILA configuration, or null if WAILA should not be shown for this block.
     */
    open fun getWaila(player: Player): WailaDisplay? {
        return WailaDisplay(defaultWailaTranslationKey)
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
            defaultItem?.getItemStack()
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
    open fun getPickItem() = defaultItem?.getItemStack()

    /**
     * Returns the item that should be used to display the block's texture.
     *
     * By default, returns the item with the same key as the block.
     *
     * @return the item that should be used to display the block's texture
     */
    open fun getBlockTextureItem() = defaultItem?.getItemStack()?.apply {
        itemMeta.persistentDataContainer.set(pylonBlockTextureEntityKey, PylonSerializers.BOOLEAN, true)
    }

    /**
     * Called when debug info is requested for the block by someone
     * using the [DebugWaxedWeatheredCutCopperStairs]. If there is
     * any transient data that can be useful for debugging, you're
     * encouraged to serialize it here.
     *
     * Defaults to a normal [write] call.
     */
    open fun writeDebugInfo(pdc: PersistentDataContainer) = write(pdc)

    /**
     * Called when the block is saved.
     *
     * Put any logic to save the data in the block here.
     *
     * *Do not assume that when this is called, the block is being unloaded.* This
     * may be called for other reasons, such as when a player right clicks with
     * [DebugWaxedWeatheredCutCopperStairs].
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

        private val pylonBlockTextureEntityKey = pylonKey("pylon_block_texture_entity")
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