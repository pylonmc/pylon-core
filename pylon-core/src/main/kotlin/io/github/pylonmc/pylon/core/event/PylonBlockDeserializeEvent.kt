package io.github.pylonmc.pylon.core.event

import io.github.pylonmc.pylon.core.block.PylonBlock
import org.bukkit.block.Block
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.persistence.PersistentDataContainer

/**
 * Called after deserializing a block
 */
class PylonBlockDeserializeEvent(
    val block: Block,
    val pylonBlock: PylonBlock,
    val pdc: PersistentDataContainer
) : Event(){

    override fun getHandlers(): HandlerList = handlerList

    companion object {
        @JvmStatic
        val handlerList: HandlerList = HandlerList()
    }
}