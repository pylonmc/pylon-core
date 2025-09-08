package io.github.pylonmc.pylon.core.recipe

import io.github.pylonmc.pylon.core.fluid.PylonFluid
import io.github.pylonmc.pylon.core.util.itemFromKey
import io.github.pylonmc.pylon.core.util.itemKey
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.Tag
import org.bukkit.inventory.ItemStack

sealed interface RecipeInput {
    data class Item(val items: MutableSet<NamespacedKey>, val amount: Int) : RecipeInput {
        constructor(amount: Int, vararg items: ItemStack) : this(items.mapTo(mutableSetOf(), ItemStack::itemKey), amount)
        constructor(amount: Int, vararg keys: NamespacedKey) : this(keys.toMutableSet(), amount)
        constructor(tag: Tag<*>, amount: Int) : this(tag.values.mapTo(mutableSetOf(), Keyed::getKey), amount)

        init {
            require(amount > 0) { "Amount must be greater than zero, but was $amount" }
            require(items.isNotEmpty()) { "Items set must not be empty" }
        }

        val representativeItems: Set<ItemStack> by lazy {
            items.mapTo(mutableSetOf()) { itemFromKey(it)!!.asQuantity(amount) }
        }

        val representativeItem: ItemStack by lazy {
            representativeItems.first()
        }

        fun matches(itemStack: ItemStack): Boolean {
            if (itemStack.amount < amount) return false
            return itemStack.itemKey in items
        }
    }

    data class Fluid(val fluids: MutableSet<PylonFluid>, val amountMillibuckets: Double) : RecipeInput {
        constructor(amountMillibuckets: Double, vararg fluids: PylonFluid) : this(fluids.toMutableSet(), amountMillibuckets)
        constructor(amountMillibuckets: Double, tag: Tag<*>) : this(tag.values.mapTo(mutableSetOf()) { it as PylonFluid }, amountMillibuckets)

        init {
            require(amountMillibuckets > 0) { "Amount in millibuckets must be greater than zero, but was $amountMillibuckets" }
            require(fluids.isNotEmpty()) { "Fluids set must not be empty" }
        }
    }

    companion object {
        @JvmStatic
        @JvmOverloads
        fun of(item: ItemStack, amount: Int = item.amount) = Item(amount, item)

        @JvmStatic
        fun of(fluid: PylonFluid, amountMillibuckets: Double) = Fluid(amountMillibuckets, fluid)
    }
}