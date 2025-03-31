package io.github.pylonmc.pylon.core.debug

import io.github.pylonmc.pylon.core.block.TickManager
import io.github.pylonmc.pylon.core.block.base.Ticking
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.item.PylonItemSchema
import io.github.pylonmc.pylon.core.item.base.BlockInteractor
import io.github.pylonmc.pylon.core.persistence.blockstorage.BlockStorage
import io.github.pylonmc.pylon.core.pluginInstance
import io.github.pylonmc.pylon.core.util.plus
import io.github.pylonmc.pylon.core.util.pylonKey
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

object DebugWaxedWeatheredCutCopperStairs : PylonItemSchema(
    pylonKey("debug_waxed_weathered_cut_copper_stairs"),
    ItemInstance::class.java,
    pluginInstance
) {

    class ItemInstance(schema: DebugWaxedWeatheredCutCopperStairs, item: ItemStack) :
        PylonItem<DebugWaxedWeatheredCutCopperStairs>(schema, item), BlockInteractor {
        override fun onUsedToClickBlock(event: PlayerInteractEvent) {
            val block = event.clickedBlock ?: return
            val pylonBlock = BlockStorage.get(block) ?: return
            val player = event.player
            player.sendMessage(NamedTextColor.GOLD + "Pylon block key: ${pylonBlock.schema.key}")
            player.sendMessage(
                MiniMessage.miniMessage().deserialize(
                    when (pylonBlock) {
                        is Ticking -> if (TickManager.isTicking(pylonBlock)) {
                            "<gold>Ticking: <green>Yes"
                        } else {
                            "<gold>Ticking: <red>Ticker has errored"
                        }

                        else -> "<gold>Ticking: <red>No"
                    }
                )
            )
            pylonBlock.write(PrintingPDC(player))
        }
    }
}