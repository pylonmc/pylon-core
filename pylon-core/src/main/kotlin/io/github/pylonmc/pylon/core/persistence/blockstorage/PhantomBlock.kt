package io.github.pylonmc.pylon.core.persistence.blockstorage

import io.github.pylonmc.pylon.core.block.BlockCreateContext
import io.github.pylonmc.pylon.core.block.PylonBlock
import io.github.pylonmc.pylon.core.block.PylonBlockSchema
import io.github.pylonmc.pylon.core.pluginInstance
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
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
    block: Block
) : PylonBlock<PylonBlockSchema>(schema, block) {

    // TODO implement breakable block interface and add some logic to drop an error item when this is broken

    // Hacky placeholder
    internal constructor(schema: PylonBlockSchema, block: Block, context: BlockCreateContext)
            : this(block.chunk.persistentDataContainer.adapterContext.newPersistentDataContainer(), block) {
        throw IllegalStateException("Phantom block cannot be placed")
    }

    // Hacky placeholder
    internal constructor(schema: PylonBlockSchema, block: Block, pdc: PersistentDataContainer)
            : this(block.chunk.persistentDataContainer.adapterContext.newPersistentDataContainer(), block) {
        throw IllegalStateException("Phantom block cannot be loaded")
    }

    companion object {
        private val key = NamespacedKey(pluginInstance, "phantom_block")

        // Intentionally not registered to hide Pylon internals
        internal val schema = PylonBlockSchema(key, Material.BARRIER, PhantomBlock::class.java)
    }
}