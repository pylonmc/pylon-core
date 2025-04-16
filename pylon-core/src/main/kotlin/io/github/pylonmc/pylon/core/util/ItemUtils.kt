package io.github.pylonmc.pylon.core.util

import io.github.pylonmc.pylon.core.item.PylonItem
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

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

fun isItemSimilar(item1: ItemStack?, item2: ItemStack?): Boolean {
    // Both items null
    if (item1 == null && item2 == null) {
        return true
    }

    // One item null, one not null
    if (!(item1 != null && item2 != null)) {
        return false
    }

    val pylonItem1 = PylonItem.fromStack(item1)
    val pylonItem2 = PylonItem.fromStack(item2)

    // Both pylon items null
    if (pylonItem1 == null && pylonItem2 == null) {
        return item1.isSimilar(item2)
    }

    // One pylon item null, one not null
    if (!(pylonItem1 != null && pylonItem2 != null)) {
        return false
    }

    return pylonItem1.id == pylonItem2.id
}