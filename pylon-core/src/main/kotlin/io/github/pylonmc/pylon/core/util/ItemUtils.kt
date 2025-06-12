@file:JvmName("ItemUtils")

package io.github.pylonmc.pylon.core.util

import io.github.pylonmc.pylon.core.item.PylonItem
import io.papermc.paper.datacomponent.DataComponentType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice

fun findPylonItemInInventory(inventory: Inventory, targetItem: PylonItem): Int? {
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

fun ItemStack?.isPylonSimilar(item2: ItemStack?): Boolean {
    // Both items null
    if (this == null && item2 == null) {
        return true
    }

    // One item null, one not null
    if (!(this != null && item2 != null)) {
        return false
    }

    val pylonItem1 = PylonItem.fromStack(this)
    val pylonItem2 = PylonItem.fromStack(item2)

    // Both pylon items null
    if (pylonItem1 == null && pylonItem2 == null) {
        return this.isSimilar(item2)
    }

    // One pylon item null, one not null
    if (!(pylonItem1 != null && pylonItem2 != null)) {
        return false
    }

    return pylonItem1.schema.key == pylonItem2.schema.key
}

fun ItemStack.asRecipeChoice(): RecipeChoice {
    return if (PylonItem.isPylonItem(this)) {
        RecipeChoice.ExactChoice(this)
    } else {
        RecipeChoice.MaterialChoice(this.type)
    }
}

@JvmSynthetic
@Suppress("UnstableApiUsage")
inline fun <T : Any> ItemStack.editData(
    type: DataComponentType.Valued<T>,
    block: (T) -> T
): ItemStack {
    val data = getData(type) ?: return this
    setData(type, block(data))
    return this
}