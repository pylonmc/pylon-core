package io.github.pylonmc.pylon.core.event

import io.github.pylonmc.pylon.core.block.PylonBlock
import org.bukkit.block.Block
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.persistence.PersistentDataContainer

/**
 * Called after serializing a block. **A block being serialized does not necessarily mean
 * it is going to be unloaded.**
 */
class PylonBlockSerializeEvent(
    val block: Block,
    val pylonBlock: PylonBlock<*>,
    val data: PersistentDataContainer
) : Event(){

    override fun getHandlers(): HandlerList = handlerList

    companion object {
        @JvmStatic
        val handlerList: HandlerList = HandlerList()
    }
}