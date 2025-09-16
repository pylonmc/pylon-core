package io.github.pylonmc.pylon.core.config.adapter

import io.github.pylonmc.pylon.core.item.ItemTypeWrapper
import io.github.pylonmc.pylon.core.item.ItemTypeWrapper.Companion.toItemTypeTag
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Tag

object ItemTagConfigAdapter : ConfigAdapter<Tag<ItemTypeWrapper>> {
    override val type = Tag::class.java

    override fun convert(value: Any): Tag<ItemTypeWrapper> {
        val string = ConfigAdapter.STRING.convert(value)
        if (!string.startsWith("#")) {
            throw IllegalArgumentException("Item tag must start with '#': $value")
        }
        val tagKey = NamespacedKey.fromString(string.drop(1)) ?: throw IllegalArgumentException("Invalid tag: $value")
        val tag = Bukkit.getTag("items", tagKey, Material::class.java)
        if (tag != null) {
            return tag.toItemTypeTag()
        }
        val pylonTag = PylonRegistry.ITEM_TAGS[tagKey]
        if (pylonTag != null) {
            return pylonTag
        }
        throw IllegalArgumentException("Item tag not found: $value")
    }
}