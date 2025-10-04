package io.github.pylonmc.pylon.core.block

import io.github.pylonmc.pylon.core.block.context.BlockBreakContext
import io.github.pylonmc.pylon.core.block.context.BlockCreateContext
import io.github.pylonmc.pylon.core.block.waila.WailaConfig
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.i18n.PylonArgument
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.util.pylonKey
import net.kyori.adventure.bossbar.BossBar
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer

/**
 * Phantom blocks are used where a block failed to load, or where a block errors and
 * is unloaded.
 *
 * The intention behind phantom blocks is to make [BlockStorage] act consistently even
 * if the block is not loaded - i.e., if a block is broken, its block storage data should also be
 * deleted. Additionally, phantom blocks allow us to persist data from blocks that have
 * failed to load. In such cases, the data should not be deleted to avoid cases where an
 * addon fails to load and all of its blocks get deleted when their chunk is loaded.
 *
 * This is slightly hacky, but also by far the simplest way to accomplish this; a more 'clean'
 * solution likely involves a lot more boilerplate and overhead, this is nice and intuitive and
 * unlikely to clash with any changes in the future.
 */
class PhantomBlock(
    val pdc: PersistentDataContainer,
    val erroredBlockKey: NamespacedKey,
    block: Block
) : PylonBlock(block) {

    // Hacky placeholder
    @Suppress("unused")
    internal constructor(block: Block, context: BlockCreateContext)
            : this(block.chunk.persistentDataContainer.adapterContext.newPersistentDataContainer(), pylonKey("bruh"), block) {
        throw UnsupportedOperationException("Phantom block cannot be placed")
    }

    // Hacky placeholder
    @Suppress("unused")
    internal constructor(block: Block, pdc: PersistentDataContainer)
            : this(block.chunk.persistentDataContainer.adapterContext.newPersistentDataContainer(), pylonKey("bruh"), block) {
        throw UnsupportedOperationException("Phantom block cannot be loaded")
    }

    override fun getWaila(player: Player): WailaConfig? {
        return WailaConfig(
            text = defaultWailaTranslationKey.arguments(PylonArgument.of("block", erroredBlockKey.toString())),
            color = BossBar.Color.RED
        )
    }

    override fun getDropItem(context: BlockBreakContext) = ErrorItem(erroredBlockKey).stack

    override fun getPickItem() = ErrorItem(erroredBlockKey).stack

    companion object {
        @JvmSynthetic
        internal val key = pylonKey("phantom_block")

        // Intentionally not registered to hide Pylon internals
        @JvmSynthetic
        internal val schema = PylonBlockSchema(key, Material.BARRIER, PhantomBlock::class.java)
    }

    internal class ErrorItem(stack: ItemStack) : PylonItem(stack) {

        constructor(erroredBlock: NamespacedKey): this(STACK.clone()) {
            stack.editPersistentDataContainer { pdc -> pdc.set(BLOCK_KEY, PylonSerializers.NAMESPACED_KEY, erroredBlock) }
        }

        override fun getPlaceholders(): List<PylonArgument> {
            val block = stack.persistentDataContainer.get(BLOCK_KEY, PylonSerializers.NAMESPACED_KEY)
                ?: return emptyList()
            return listOf(PylonArgument.of("block", block.toString()))
        }

        companion object {
            val KEY = pylonKey("error_item")
            val BLOCK_KEY = pylonKey("block")
            val STACK = ItemStackBuilder.pylon(Material.BARRIER, KEY)
                .build()
        }
    }
}