@file:JvmName("ItemUtils")

package io.github.pylonmc.pylon.core.util

import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack

val ItemStack.pylonKey: NamespacedKey
    get() {
        val pylonItem = PylonItem.fromStack(this)
        return pylonItem?.schema?.key ?: this.type.key
    }

private val keyToMaterial = Material.entries.associateBy { it.key }

fun pylonKeyToItem(key: NamespacedKey): ItemStack {
    val pylonItem = PylonRegistry.ITEMS[key]?.itemStack
    return if (pylonItem != null) {
        pylonItem
    } else {
        val material = keyToMaterial[key] ?: throw IllegalArgumentException("Unknown item: $key")
        ItemStack(material)
    }
}