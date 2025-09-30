package io.github.pylonmc.pylon.core.item.base

import io.papermc.paper.event.block.BlockPreDispenseEvent
import org.bukkit.event.block.BlockDispenseEvent

interface PylonDispensable {
    fun onPreDispense(event: BlockPreDispenseEvent)
    fun onDispense(event: BlockDispenseEvent)
}