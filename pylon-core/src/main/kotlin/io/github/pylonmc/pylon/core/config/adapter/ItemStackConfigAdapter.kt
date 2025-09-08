package io.github.pylonmc.pylon.core.config.adapter

import io.github.pylonmc.pylon.core.util.itemFromKey
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack

object ItemStackConfigAdapter : ConfigAdapter<ItemStack> {

    override val type = ItemStack::class.java

    override fun convert(value: Any): ItemStack {
        return when (value) {
            is Pair<*, *> -> {
                val itemKey = ConfigAdapter.STRING.convert(value.first!!)
                val amount = ConfigAdapter.INT.convert(value.second!!)
                convert(itemKey).asQuantity(amount)
            }

            is ConfigurationSection, is Map<*, *> -> convert(SectionOrMap.of(value).asMap().toList().single())
            is String -> itemFromKey(value) ?: throw IllegalArgumentException("Cannot find item $value")
            else -> throw IllegalArgumentException("Cannot convert $value to ItemStack")
        }
    }
}