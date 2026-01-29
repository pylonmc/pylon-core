package io.github.pylonmc.rebar.config.adapter

import io.github.pylonmc.rebar.recipe.FluidOrItem
import org.bukkit.configuration.ConfigurationSection

object FluidOrItemConfigAdapter : ConfigAdapter<FluidOrItem> {

    override val type = FluidOrItem::class.java

    override fun convert(value: Any): FluidOrItem {
        val item = runCatching { ConfigAdapter.ITEM_STACK.convert(value) }.getOrNull()
        return if (item != null) {
            FluidOrItem.of(item)
        } else when (value) {
            is Pair<*, *> -> {
                val fluid = ConfigAdapter.REBAR_FLUID.convert(value.first!!)
                val amount = ConfigAdapter.DOUBLE.convert(value.second!!)
                FluidOrItem.of(fluid, amount)
            }

            is ConfigurationSection, is Map<*, *> -> convert(MapConfigAdapter.STRING_TO_ANY.convert(value).toList().single())
            else -> throw IllegalArgumentException("Cannot convert $value to FluidOrItem")
        }
    }
}