package io.github.pylonmc.rebar.event

import io.github.pylonmc.rebar.block.RebarBlock
import io.github.pylonmc.rebar.block.context.BlockBreakContext
import org.bukkit.block.Block
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.inventory.ItemStack

/**
 * Called after a [RebarBlock] has been broken.
 *
 * @see BlockBreakContext
 */
class RebarBlockBreakEvent(
    val block: Block,
    val rebarBlock: RebarBlock,
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