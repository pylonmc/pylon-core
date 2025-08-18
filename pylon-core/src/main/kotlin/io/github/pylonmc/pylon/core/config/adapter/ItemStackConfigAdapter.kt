package io.github.pylonmc.pylon.core.config.adapter

import io.github.pylonmc.pylon.core.util.itemFromName
import org.bukkit.inventory.ItemStack

object ItemStackConfigAdapter : ConfigAdapter<ItemStack> {

    override val type = ItemStack::class.java

    override fun convert(value: Any): ItemStack {
        return when (value) {
            is Pair<*, *> -> {
                val itemKey = ConfigAdapter.STRING.convert(value.first!!)
                val amount = ConfigAdapter.INT.convert(value.second!!)
                itemFromName(itemKey)!!.asQuantity(amount)
            }

            is Map<*, *> -> convert(value.toList().single())
            is String -> itemFromName(value)!!
            else -> throw IllegalArgumentException("Cannot convert $value to ItemStack")
        }
    }
}