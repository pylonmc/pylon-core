package io.github.pylonmc.pylon.core.item

import io.papermc.paper.datacomponent.DataComponentBuilder
import io.papermc.paper.datacomponent.DataComponentType
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.EnumSet


@Suppress("UnstableApiUsage")
open class ItemStackBuilder(private val stack: ItemStack) {

    constructor(material: Material) : this(ItemStack(material))

    fun amount(amount: Int) = apply {
        stack.amount = amount
    }

    fun <T : Any> set(type: DataComponentType.Valued<T>, valueBuilder: DataComponentBuilder<T>) = apply {
        stack.setData(type, valueBuilder)
    }

    fun <T : Any> set(type: DataComponentType.Valued<T>, value: T) = apply {
        stack.setData(type, value)
    }

    fun set(type: DataComponentType.NonValued) = apply {
        stack.setData(type)
    }

    fun unset(type: DataComponentType.NonValued) = apply {
        stack.unsetData(type)
    }

    fun reset(type: DataComponentType.NonValued) = apply {
        stack.unsetData(type)
    }

    fun name(name: Component) = set(DataComponentTypes.ITEM_NAME, name)

    fun name(name: String) = name(fromMiniMessage(name))

    fun lore(vararg lore: Component) = apply {
        val loreBuilder = ItemLore.lore()
        for (line in lore) {
            loreBuilder.addLine(
                Component.empty()
                    .decorations(EnumSet.allOf(TextDecoration::class.java), false)
                    .color(NamedTextColor.GRAY)
                    .append(line)
            )
        }
        stack.setData(DataComponentTypes.LORE, loreBuilder)
    }

    fun lore(vararg lore: String) = lore(*lore.map(::fromMiniMessage).toTypedArray())

    fun build(): ItemStack = stack.clone()
}

private fun fromMiniMessage(string: String) = MiniMessage.miniMessage().deserialize(string)