package io.github.pylonmc.pylon.core.recipe

import io.github.pylonmc.pylon.core.fluid.PylonFluid
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice

sealed interface FluidOrItem {

    @JvmRecord
    data class Item(val item: ItemStack) : FluidOrItem

    @JvmRecord
    data class Fluid(val fluid: PylonFluid, val amountMillibuckets: Double) : FluidOrItem

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
                else -> emptyList()
            }
    }
}