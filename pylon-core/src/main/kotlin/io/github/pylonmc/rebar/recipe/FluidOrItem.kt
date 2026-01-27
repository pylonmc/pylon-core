package io.github.pylonmc.rebar.recipe

import io.github.pylonmc.rebar.fluid.PylonFluid
import io.github.pylonmc.rebar.item.ItemTypeWrapper
import org.bukkit.Keyed
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice

sealed interface FluidOrItem : Keyed {

    fun matchesType(other: FluidOrItem): Boolean

    @JvmRecord
    data class Item(val item: ItemStack) : FluidOrItem {
        override fun getKey() = ItemTypeWrapper(item).key
        override fun matchesType(other: FluidOrItem) = other is Item && ItemTypeWrapper(this.item) == ItemTypeWrapper(other.item)
    }

    @JvmRecord
    data class Fluid(val fluid: PylonFluid, val amountMillibuckets: Double) : FluidOrItem {
        override fun getKey() = fluid.key
        override fun matchesType(other: FluidOrItem) = other is Fluid && this.fluid == other.fluid
    }

    companion object {

        @JvmStatic
        fun of(item: ItemStack): FluidOrItem = Item(item)

        @JvmStatic
        fun of(fluid: PylonFluid, amountMillibuckets: Double): FluidOrItem = Fluid(fluid, amountMillibuckets)

        @JvmStatic
        fun of(choice: RecipeChoice): List<FluidOrItem>
            = when (choice) {
                is RecipeChoice.ExactChoice -> listOf(of(choice.itemStack))
                is RecipeChoice.MaterialChoice -> listOf(of(choice.itemStack))
                else -> throw AssertionError()
            }
    }
}