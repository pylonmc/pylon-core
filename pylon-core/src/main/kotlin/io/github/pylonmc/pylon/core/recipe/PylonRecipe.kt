package io.github.pylonmc.pylon.core.recipe

import io.github.pylonmc.pylon.core.fluid.PylonFluid
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.util.isPylonSimilar
import org.bukkit.Keyed
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice
import xyz.xenondevs.invui.gui.Gui

interface PylonRecipe : Keyed {
    fun isHidden() = false
    fun getInputItems(): List<RecipeChoice>
    fun getInputFluids(): List<PylonFluid> = listOf()
    fun getOutputItems(): List<ItemStack>
    fun getOutputFluids(): List<PylonFluid> = listOf()
    // I have found it's very easy to return null input item by accident, hence the seemingly redundant null check
    fun isInput(stack: ItemStack) = getInputItems().any {
        if (it is RecipeChoice.MaterialChoice && PylonItem.fromStack(stack) != null) {
            return false
        }
        it != null && it.test(stack)
    }
    fun isInput(fluid: PylonFluid) = getInputFluids().contains(fluid)
    fun isOutput(stack: ItemStack) = getOutputItems().any { stack.isPylonSimilar(it) }
    fun isOutput(fluid: PylonFluid) = getOutputFluids().contains(fluid)
    fun display(): Gui
}
