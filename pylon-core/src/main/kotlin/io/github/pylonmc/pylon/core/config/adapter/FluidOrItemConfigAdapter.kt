package io.github.pylonmc.pylon.core.config.adapter

import io.github.pylonmc.pylon.core.recipe.FluidOrItem

object FluidOrItemConfigAdapter : ConfigAdapter<FluidOrItem> {

    override val type = FluidOrItem::class.java

    override fun convert(value: Any): FluidOrItem {
        val item = runCatching { ConfigAdapter.ITEM_STACK.convert(value) }.getOrNull()
        return if (item != null) {
            FluidOrItem.of(item)
        } else when (value) {
            is Pair<*, *> -> {
                val fluid = ConfigAdapter.PYLON_FLUID.convert(value.first!!)
                val amount = ConfigAdapter.DOUBLE.convert(value.second!!)
                FluidOrItem.of(fluid, amount)
            }

            is Map<*, *> -> convert(value.toList().single())
            else -> throw IllegalArgumentException("Cannot convert $value to FluidOrItem")
        }
    }
}