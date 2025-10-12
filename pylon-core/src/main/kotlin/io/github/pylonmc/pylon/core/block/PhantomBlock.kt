package io.github.pylonmc.pylon.core.block

import io.github.pylonmc.pylon.core.block.base.PylonBreakHandler
import io.github.pylonmc.pylon.core.block.base.PylonEntityHolderBlock
import io.github.pylonmc.pylon.core.block.context.BlockBreakContext
import io.github.pylonmc.pylon.core.block.context.BlockCreateContext
import io.github.pylonmc.pylon.core.block.waila.WailaConfig
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.entity.display.transform.TransformBuilder
import io.github.pylonmc.pylon.core.i18n.PylonArgument
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.util.pylonKey
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.bossbar.BossBar
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.entity.Display
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import java.util.UUID

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
) : PylonBlock(block), PylonBreakHandler {

    override var disableBlockTextureEntity: Boolean = true
    private var errorOutlineEntityId : UUID? = null

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

    init {
        errorOutlineEntityId = block.world.spawn(block.location.toCenterLocation(), ItemDisplay::class.java) { display ->
            display.setItemStack(ItemStackBuilder.of(Material.BARRIER).set(DataComponentTypes.ITEM_MODEL, PhantomBlock.key).build())
            display.glowColorOverride = Color.RED
            display.isGlowing = true
            display.isPersistent = false
            display.brightness = Display.Brightness(15, 15)
            display.setTransformationMatrix(TransformBuilder().scale(1.001f).buildForItemDisplay())
        }.uniqueId
    }

    override fun postBreak(context: BlockBreakContext) {
        errorOutlineEntityId?.let { uuid ->
            block.world.getEntity(uuid)?.remove()
        }
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
            val STACK = ItemStackBuilder.pylonItem(Material.CLAY_BALL, KEY)
                .set(DataComponentTypes.ITEM_MODEL, Material.BARRIER.key)
                .build()
        }
    }
}