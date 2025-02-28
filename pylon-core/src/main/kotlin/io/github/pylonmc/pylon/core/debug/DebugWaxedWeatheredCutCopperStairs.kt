package io.github.pylonmc.pylon.core.debug

import io.github.pylonmc.pylon.core.item.ItemStackBuilder
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.item.PylonItemSchema
import io.github.pylonmc.pylon.core.item.base.BlockInteractor
import io.github.pylonmc.pylon.core.persistence.blockstorage.BlockStorage
import io.github.pylonmc.pylon.core.util.plus
import io.github.pylonmc.pylon.core.util.pylonKey
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

object DebugWaxedWeatheredCutCopperStairs : PylonItemSchema(
    pylonKey("debug_waxed_weathered_cut_copper_stairs"),
    ItemInstance::class.java,
    ItemStackBuilder(Material.WAXED_WEATHERED_CUT_COPPER_STAIRS)
        .name("<red>Debug Waxed Weathered Cut Copper Stairs")
        .lore("Right click a block to view its Pylon block data")
        .build(),
) {

    class ItemInstance(schema: DebugWaxedWeatheredCutCopperStairs, item: ItemStack) :
        PylonItem<DebugWaxedWeatheredCutCopperStairs>(schema, item), BlockInteractor {
        override fun onUsedToRightClickBlock(event: PlayerInteractEvent) {
            val block = event.clickedBlock ?: return
            val pylonBlock = BlockStorage.get(block) ?: return
            event.player.sendMessage(NamedTextColor.GOLD + "Pylon block key: ${pylonBlock.schema.key}")
            pylonBlock.write(PrintingPDC(event.player))
        }
    }
}