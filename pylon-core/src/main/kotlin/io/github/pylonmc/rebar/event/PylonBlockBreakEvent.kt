package io.github.pylonmc.rebar.event

import io.github.pylonmc.rebar.block.PylonBlock
import io.github.pylonmc.rebar.block.context.BlockBreakContext
import org.bukkit.block.Block
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.inventory.ItemStack

/**
 * Called after a [PylonBlock] has been broken.
 *
 * @see BlockBreakContext
 */
class PylonBlockBreakEvent(
    val block: Block,
    val pylonBlock: PylonBlock,
    val context: BlockBreakContext,
    val drops: MutableList<ItemStack>
) : Event(){

    override fun getHandlers(): HandlerList
        = handlerList

    companion object {
        @JvmStatic
        val handlerList: HandlerList = HandlerList()
    }
}