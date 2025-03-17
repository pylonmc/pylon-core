@file:JvmName("KeyUtils")

package io.github.pylonmc.pylon.core.util.key

import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.persistence.blockstorage.BlockStorage
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack

val ItemStack.pylonItemKey: NamespacedKey
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

var Block.pylonType: NamespacedKey
    get() {
        val pylonBlock = BlockStorage.get(this)
        return pylonBlock?.schema?.key ?: this.type.key
    }
    set(key) {
        val schema = PylonRegistry.BLOCKS[key]
        if (schema != null) {
            BlockStorage.placeBlock(this, schema)
        } else {
            val material = keyToMaterial[key] ?: throw IllegalArgumentException("Unknown block: $key")
            this.type = material
        }
    }
