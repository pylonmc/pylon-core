package io.github.pylonmc.pylon.core.config.adapter

import io.github.pylonmc.pylon.core.recipe.FluidOrItem
import io.github.pylonmc.pylon.core.recipe.RecipeInput
import org.bukkit.configuration.ConfigurationSection

object RecipeInputConfigAdapter : ConfigAdapter<RecipeInput> {

    override val type = FluidOrItem::class.java

    override fun convert(value: Any): RecipeInput {
        val item = runCatching { ConfigAdapter.ITEM_STACK.convert(value) }.getOrNull()
        return if (item != null) {
            RecipeInput.of(item)
        } else when (value) {
            is Pair<*, *> -> {
                val fluid = ConfigAdapter.PYLON_FLUID.convert(value.first!!)
                val amount = ConfigAdapter.DOUBLE.convert(value.second!!)
                RecipeInput.of(fluid, amount)
            }

            is ConfigurationSection -> convert(value.getValues(false).toList().single())
            else -> throw IllegalArgumentException("Cannot convert $value to RecipeInput")
        }
    }
}