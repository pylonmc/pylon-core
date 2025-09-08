package io.github.pylonmc.pylon.core.recipe

import io.github.pylonmc.pylon.core.fluid.PylonFluid
import io.github.pylonmc.pylon.core.util.isPylonSimilar
import org.bukkit.Keyed
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.gui.Gui

interface PylonRecipe : Keyed {

    val isHidden: Boolean
        get() = false

    val inputs: List<RecipeInput>
    val results: List<FluidOrItem>

    fun isInput(stack: ItemStack) = inputs.any {
        when (it) {
            is RecipeInput.Item -> it.matches(stack)
            else -> false
        }
    }

    fun isInput(fluid: PylonFluid) = inputs.any {
        when (it) {
            is RecipeInput.Fluid -> fluid in it.fluids
            else -> false
        }
    }

    fun isOutput(stack: ItemStack) = results.any {
        when (it) {
            is FluidOrItem.Item -> it.item.isPylonSimilar(stack)
            else -> false
        }
    }

    fun isOutput(fluid: PylonFluid) = results.any {
        when (it) {
            is FluidOrItem.Fluid -> it.fluid == fluid
            else -> false
        }
    }

    fun display(): Gui
}
