package io.github.pylonmc.pylon.core.guide.category

import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import net.kyori.adventure.text.Component
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.NamespacedKey
import xyz.xenondevs.invui.item.Item

class GuideCategory(
    val key: NamespacedKey,
    val material: Material,
) : Keyed {

    val buttons: MutableSet<Item> = mutableSetOf()

    val item = ItemStackBuilder.of(material)
        .name(Component.translatable("pylon.${key.namespace}.guide.category.${key.key}.name"))
        .lore(Component.translatable("pylon.${key.namespace}.guide.category.${key.key}.lore"))

    fun addItem(item: NamespacedKey) = buttons.add(ItemButton(item))
    fun addCategory(category: GuideCategory) = buttons.add(CategoryButton(category))

    override fun getKey(): NamespacedKey = key
}