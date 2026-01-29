package io.github.pylonmc.rebar.item.base

import org.bukkit.event.block.BlockDispenseEvent

interface RebarDispensable {
    fun onDispense(event: BlockDispenseEvent)
}