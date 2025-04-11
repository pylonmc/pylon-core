package io.github.pylonmc.pylon.core.debug

import io.github.pylonmc.pylon.core.block.TickManager
import io.github.pylonmc.pylon.core.block.base.Ticking
import io.github.pylonmc.pylon.core.entity.EntityStorage
import io.github.pylonmc.pylon.core.block.base.PylonTickingBlock
import io.github.pylonmc.pylon.core.item.ItemStackBuilder
import io.github.pylonmc.pylon.core.item.LoreBuilder
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.item.PylonItemSchema
import io.github.pylonmc.pylon.core.item.base.BlockInteractor
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.item.builder.LoreBuilder
import io.github.pylonmc.pylon.core.item.base.EntityInteractor
import io.github.pylonmc.pylon.core.persistence.blockstorage.BlockStorage
import io.github.pylonmc.pylon.core.util.pylonKey
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

@Suppress("UnstableApiUsage")
object DebugWaxedWeatheredCutCopperStairs : PylonItemSchema(
    pylonKey("debug_waxed_weathered_cut_copper_stairs"),
    ItemInstance::class.java,
    { key ->
        ItemStackBuilder.defaultBuilder(Material.WAXED_WEATHERED_CUT_COPPER_STAIRS, key)
            .set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
            .build()
    }
) {

    class ItemInstance(schema: DebugWaxedWeatheredCutCopperStairs, item: ItemStack) :
        PylonItem<DebugWaxedWeatheredCutCopperStairs>(schema, item), BlockInteractor, EntityInteractor {
        override fun onUsedToClickBlock(event: PlayerInteractEvent) {
            val block = event.clickedBlock ?: return
            val pylonBlock = BlockStorage.get(block)
            if (pylonBlock == null) {
                event.player.sendMessage(NamedTextColor.RED + "This is not a Pylon block")
                return
            }
            val player = event.player
            player.sendDebug("block_key", Component.text(pylonBlock.schema.key.toString()))
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
            pylonBlock.write(PrintingPDC(player))
        }

        override fun onUsedToRightClickEntity(event: PlayerInteractEntityEvent) {
            val pylonEntity = EntityStorage.get(event.rightClicked)
            if (pylonEntity == null) {
                event.player.sendMessage(NamedTextColor.RED + "This is not a Pylon entity")
                return
            }
            event.player.sendMessage(NamedTextColor.GOLD + "Pylon entity key: ${pylonEntity.schema.key}")
            event.player.sendMessage(
                MiniMessage.miniMessage().deserialize(
                    when (pylonEntity) {
                        // TODO implement this once entities can tick
                        is PylonTickingBlock -> if (false) {
                            "<gold>Ticking: <green>Yes"
                        } else {
                            "<gold>Ticking: <red>Ticker has errored"
                        }

                        else -> "<gold>Ticking: <red>No"
                    }
                )
            )
            pylonEntity.entity.persistentDataContainer.copyTo(PrintingPDC(event.player), true)
        }
    }
}

private fun Audience.sendDebug(subkey: String, vararg args: Component) {
    return sendMessage(Component.translatable("pylon.pyloncore.message.debug.$subkey", *args))
}