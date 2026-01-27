package io.github.pylonmc.rebar.item.base

import org.bukkit.event.block.BlockDispenseEvent

interface PylonDispensable {
    fun onDispense(event: BlockDispenseEvent)
}