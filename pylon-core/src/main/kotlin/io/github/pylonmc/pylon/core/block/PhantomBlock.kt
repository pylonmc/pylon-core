package io.github.pylonmc.pylon.core.block

import io.github.pylonmc.pylon.core.block.context.BlockCreateContext
import io.github.pylonmc.pylon.core.block.context.BlockItemContext
import io.github.pylonmc.pylon.core.block.waila.WailaConfig
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.item.PylonItemSchema
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.pluginInstance
import io.github.pylonmc.pylon.core.util.pylonKey
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer

/**
 * Phantom blocks are used where a block failed to load.
 *
 * The intention behind placeholder blocks is to make BlockStorage act consistently even
 * if the block is not loaded - ie, if a block is broken, its blockstorage data should also be
 * deleted. Additionally, phantom blocks allow us to persist data from blocks that have
 * failed to load. In such cases, the data should not be deleted, to avoid cases where an
 * addon fails to load and all of its blocks get deleted when their chunk is loaded.
 *
 * This is slightly hacky, but also by far the simplest way to accomplish this (a more 'clean'
 * solution likely involves a lot more boilerplate and overhead, this is nice and intuitive and
 * unlikely to clash with any changes in the future).
 */
class PhantomBlock(
    val pdc: PersistentDataContainer,
    val erroredBlockKey: NamespacedKey,
    block: Block
) : PylonBlock<PylonBlockSchema>(schema, block) {

    // Hacky placeholder
    internal constructor(schema: PylonBlockSchema, block: Block, context: BlockCreateContext)
            : this(block.chunk.persistentDataContainer.adapterContext.newPersistentDataContainer(), schema.key, block) {
        throw UnsupportedOperationException("Phantom block cannot be placed")
    }

    // Hacky placeholder
    internal constructor(schema: PylonBlockSchema, block: Block, pdc: PersistentDataContainer)
            : this(block.chunk.persistentDataContainer.adapterContext.newPersistentDataContainer(), schema.key, block) {
        throw UnsupportedOperationException("Phantom block cannot be loaded")
    }

    override fun getWaila(player: Player): WailaConfig {
        return WailaConfig(
            text = name,
            placeholders = mapOf("block" to Component.text(erroredBlockKey.toString())),
            color = BossBar.Color.RED
        )
    }

    override fun getItem(context: BlockItemContext): ItemStack? {
        val item = ErrorItem.Schema.itemStack
        item.editMeta {
            it.persistentDataContainer.set(ErrorItem.blockKey, PylonSerializers.NAMESPACED_KEY, erroredBlockKey)
        }
        return item
    }

    companion object {
        internal val key = NamespacedKey(pluginInstance, "phantom_block")

        // Intentionally not registered to hide Pylon internals
        @JvmSynthetic
        internal val schema = PylonBlockSchema(key, Material.BARRIER, PhantomBlock::class.java)
    }

    class ErrorItem(schema: Schema, stack: ItemStack) : PylonItem<ErrorItem.Schema>(schema, stack) {

        companion object Schema : PylonItemSchema(
            pylonKey("error_item"),
            ErrorItem::class.java,
            { key ->
                ItemStackBuilder.defaultBuilder(Material.BARRIER, key).build()
            }
        ) {
            val blockKey = pylonKey("block")
        }

        override fun getPlaceholders(): Map<String, Component> {
            val block = stack.persistentDataContainer.get(blockKey, PylonSerializers.NAMESPACED_KEY)
                ?: return emptyMap()
            return mapOf("block" to Component.text(block.toString()))
        }
    }
}