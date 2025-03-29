package io.github.pylonmc.pylon.core.persistence.blockstorage

import io.github.pylonmc.pylon.core.block.BlockCreateContext
import io.github.pylonmc.pylon.core.block.BlockItemReason
import io.github.pylonmc.pylon.core.block.PylonBlock
import io.github.pylonmc.pylon.core.block.PylonBlockSchema
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.pluginInstance
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
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
    val key: NamespacedKey,
    block: Block
) : PylonBlock<PylonBlockSchema>(schema, block) {

    // Hacky placeholder
    internal constructor(schema: PylonBlockSchema, block: Block, context: BlockCreateContext)
            : this(block.chunk.persistentDataContainer.adapterContext.newPersistentDataContainer(), schema.key, block) {
        throw IllegalStateException("Phantom block cannot be placed")
    }

    // Hacky placeholder
    internal constructor(schema: PylonBlockSchema, block: Block, pdc: PersistentDataContainer)
            : this(block.chunk.persistentDataContainer.adapterContext.newPersistentDataContainer(), schema.key, block) {
        throw IllegalStateException("Phantom block cannot be loaded")
    }

    override fun getItem(reason: BlockItemReason): ItemStack? {
        val item = errorItem.clone()
        item.editMeta {
            val lore = item.lore() ?: mutableListOf()
            lore.add(
                MiniMessage.miniMessage().deserialize(
                    "<red>Errored block: <yellow>${schema.key}"
                )
            )
            it.lore(lore)
        }
        return item
    }

    companion object {
        private val key = NamespacedKey(pluginInstance, "phantom_block")

        private val errorItem = ItemStackBuilder(Material.ECHO_SHARD)
            .name("<red>Error")
            .lore(
                "<red>This item dropped from a",
                "<red>block that failed to load.",
                "<red>ID:</red> <yellow>$key"
            )
            .set(DataComponentTypes.ITEM_MODEL, Material.BARRIER.key)
            .build()

        // Intentionally not registered to hide Pylon internals
        @JvmSynthetic
        internal val schema = PylonBlockSchema(key, Material.BARRIER, PhantomBlock::class.java)
    }
}