package io.github.pylonmc.pylon.core.util

import io.github.pylonmc.pylon.core.item.PylonItem
import org.bukkit.inventory.Inventory

fun findPylonItemInInventory(inventory: Inventory, targetItem: PylonItem<*>): Int? {
    for (i in 0..<inventory.size) {
        val item = inventory.getItem(i)?.let {
            PylonItem.fromStack(it)
        }
        if (item == targetItem) {
            return i
        }
    }
    return null
}
