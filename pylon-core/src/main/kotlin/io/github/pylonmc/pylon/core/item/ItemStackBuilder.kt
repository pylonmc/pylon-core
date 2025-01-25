package io.github.pylonmc.pylon.core.item

import io.papermc.paper.datacomponent.DataComponentBuilder
import io.papermc.paper.datacomponent.DataComponentType
import org.bukkit.Material
import org.bukkit.inventory.ItemStack


@Suppress("UnstableApiUsage")
open class ItemStackBuilder(private val stack: ItemStack) {
    constructor(material: Material) : this(ItemStack(material))

    fun amount(amount: Int) = apply {
        stack.amount = amount
    }

    fun <T: Any> set(type: DataComponentType.Valued<T>, valueBuilder: DataComponentBuilder<T>) = apply {
        stack.setData(type, valueBuilder)
    }

    fun <T: Any> set(type: DataComponentType.Valued<T>, value: T) = apply {
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

    fun build()
        = stack.clone()
}