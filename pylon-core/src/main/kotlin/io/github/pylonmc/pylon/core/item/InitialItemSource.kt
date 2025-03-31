package io.github.pylonmc.pylon.core.item

import io.github.pylonmc.pylon.core.config.saveItemToYaml
import org.bukkit.NamespacedKey
import org.bukkit.plugin.Plugin
import kotlin.io.path.outputStream
import kotlin.io.path.writer
import org.bukkit.inventory.ItemStack as BukkitItemStack

sealed interface InitialItemSource {

    fun createItemFile(key: NamespacedKey)

    data class ItemStack(val item: BukkitItemStack) : InitialItemSource {
        override fun createItemFile(key: NamespacedKey) {
            val itemPath = PylonItemSchema.getItemConfigPath(key)
            itemPath.writer().use { saveItemToYaml(item, it) }
        }
    }

    data class File @JvmOverloads constructor(val plugin: Plugin, val path: String? = null) : InitialItemSource {
        override fun createItemFile(key: NamespacedKey) {
            val path = this.path ?: "items/${key.namespace}/${key.key}.yml"
            val resource = plugin.getResource(path)
                ?: error("Failed to load item template for $key; file not found at $path")
            val itemPath = PylonItemSchema.getItemConfigPath(key)
            resource.use { input ->
                itemPath.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
    }
}