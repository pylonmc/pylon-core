package io.github.pylonmc.pylon.core.item.base

import org.bukkit.event.block.BlockDispenseEvent

interface PylonDispensable {
    fun onDispense(event: BlockDispenseEvent)
}