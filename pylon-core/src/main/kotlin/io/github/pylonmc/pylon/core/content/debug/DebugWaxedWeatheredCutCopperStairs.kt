package io.github.pylonmc.pylon.core.content.debug

import io.github.pylonmc.pylon.core.block.BlockStorage
import io.github.pylonmc.pylon.core.block.PylonBlock
import io.github.pylonmc.pylon.core.block.TickManager
import io.github.pylonmc.pylon.core.block.base.PylonTickingBlock
import io.github.pylonmc.pylon.core.entity.EntityStorage
import io.github.pylonmc.pylon.core.entity.PylonEntity
import io.github.pylonmc.pylon.core.event.PylonBlockSerializeEvent
import io.github.pylonmc.pylon.core.i18n.PylonArgument
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.item.base.PylonBlockInteractor
import io.github.pylonmc.pylon.core.item.base.PylonItemEntityInteractor
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.nms.NmsAccessor
import io.github.pylonmc.pylon.core.util.position.position
import io.github.pylonmc.pylon.core.util.pylonKey
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

@Suppress("UnstableApiUsage")
class DebugWaxedWeatheredCutCopperStairs(stack: ItemStack)
    : PylonItem(stack), PylonBlockInteractor, PylonItemEntityInteractor {

    override fun onUsedToClickBlock(event: PlayerInteractEvent) {
        event.isCancelled = true
        if (event.action == Action.PHYSICAL) return

        val block = event.clickedBlock ?: return
        val pylonBlock = BlockStorage.get(block)
        val player = event.player
        if (pylonBlock == null) {
            player.sendDebug("not_a_block")
            return
        }

        if (event.action.isLeftClick) {
            onUsedToLeftClickBlock(player, block, pylonBlock)
        } else if (event.action.isRightClick) {
            onUsedToRightClickBlock(player, block, pylonBlock)
        }
    }

    fun onUsedToLeftClickBlock(player: Player, block: Block, pylonBlock: PylonBlock) {

    }

    fun onUsedToRightClickBlock(player: Player, block: Block, pylonBlock: PylonBlock) {
        if (player.currentInput.isSneak) {
            BlockStorage.deleteBlockData(block.position)
            player.sendDebug(
                "deleted_data",
                PylonArgument.of("type", pylonBlock.schema.key.toString()),
                PylonArgument.of("location", block.position.toString())
            )
            return
        }

        player.sendDebug(
            "key.block",
            PylonArgument.of("key", pylonBlock.schema.key.toString())
        )
        player.sendDebug(
            when (pylonBlock) {
                is PylonTickingBlock -> if (TickManager.isTicking(pylonBlock)) {
                    "ticking.ticking"
                } else {
                    "ticking.error"
                }

                else -> "ticking.not_ticking"
            }
        )
        // Create a new PDC - doesn't matter what type because we won't be saving it, so we just use the block's
        // chunk to get a PDC context
        val pdc = block.chunk.persistentDataContainer.adapterContext.newPersistentDataContainer()
        pylonBlock.write(pdc)
        PylonBlockSerializeEvent(block, pylonBlock, pdc).callEvent()
        val serialized = NmsAccessor.instance.serializePdc(pdc)
        player.sendDebug(
            "data",
            PylonArgument.of("data", serialized)
        )
    }

    override fun onUsedToRightClickEntity(event: PlayerInteractEntityEvent) {
        val pylonEntity = EntityStorage.get(event.rightClicked)
        val player = event.player
        if (pylonEntity == null) {
            player.sendDebug("not_an_entity")
            return
        }

        if (player.currentInput.isSneak) {
            pylonEntity.entity.remove()
            player.sendDebug(
                "deleted_data",
                PylonArgument.of("type", pylonEntity.schema.key.toString()),
                PylonArgument.of("location", pylonEntity.entity.uniqueId.toString())
            )
            return
        }

        player.sendDebug(
            "key.entity",
            PylonArgument.of("key", pylonEntity.schema.key.toString())
        )

        // TODO implement this once entities can tick
//            event.player.sendMessage(
//                MiniMessage.miniMessage().deserialize(
//                    when (pylonEntity) {
//                        is PylonTickingBlock -> if (false) {
//                            "<gold>Ticking: <green>Yes"
//                        } else {
//                            "<gold>Ticking: <red>Ticker has errored"
//                        }
//
//                        else -> "<gold>Ticking: <red>No"
//                    }
//                )
//            )
        pylonEntity.write(pylonEntity.entity.persistentDataContainer)
        val serialized = NmsAccessor.instance.serializePdc(pylonEntity.entity.persistentDataContainer)
        player.sendDebug(
            "data",
            PylonArgument.of("data", serialized)
        )
    }

    companion object {
        val KEY = pylonKey("debug_waxed_weathered_cut_copper_stairs")
        val STACK = ItemStackBuilder.pylonItem(Material.BRICK, KEY)
            .set(DataComponentTypes.ITEM_MODEL, Material.WAXED_WEATHERED_CUT_COPPER_STAIRS.key)
            .set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
            .build()
    }
}

private fun Audience.sendDebug(subkey: String, vararg args: PylonArgument) {
    return sendMessage(Component.translatable("pylon.pyloncore.message.debug.$subkey", *args))
}
