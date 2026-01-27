package io.github.pylonmc.rebar.recipe

import io.github.pylonmc.rebar.fluid.PylonFluid
import org.bukkit.Keyed
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.gui.Gui

interface PylonRecipe : Keyed {

    val isHidden: Boolean
        get() = false

    val inputs: List<RecipeInput>
    val results: List<FluidOrItem>

    fun isInput(stack: ItemStack) = inputs.any { input ->
        when (input) {
            is RecipeInput.Item -> stack in input
            else -> false
        }
    }

    fun isInput(fluid: PylonFluid) = inputs.any { input ->
        when (input) {
            is RecipeInput.Fluid -> fluid in input.fluids
            else -> false
        }
    }

    fun isOutput(stack: ItemStack) = results.any {
        when (it) {
            is FluidOrItem.Item -> it.item.isSimilar(stack)
            else -> false
        }
    }

    fun isOutput(fluid: PylonFluid) = results.any {
        when (it) {
            is FluidOrItem.Fluid -> it.fluid == fluid
            else -> false
        }
    }

    fun display(): Gui?
}
