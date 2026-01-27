package io.github.pylonmc.rebar.event

import io.github.pylonmc.rebar.block.RebarBlock
import org.bukkit.block.Block
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.persistence.PersistentDataContainer

/**
 * Called after the [rebarBlock] has been deserialized.
 */
class RebarBlockDeserializeEvent(
    val block: Block,
    val rebarBlock: RebarBlock,
    val pdc: PersistentDataContainer
) : Event(){

    override fun getHandlers(): HandlerList = handlerList

    companion object {
        @JvmStatic
        val handlerList: HandlerList = HandlerList()
    }
}