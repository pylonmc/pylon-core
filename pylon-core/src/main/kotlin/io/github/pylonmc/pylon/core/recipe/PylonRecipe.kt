package io.github.pylonmc.pylon.core.recipe

import io.github.pylonmc.pylon.core.fluid.PylonFluid
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.util.isPylonSimilar
import org.bukkit.Keyed
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice
import xyz.xenondevs.invui.gui.Gui

interface PylonRecipe : Keyed {

    val isHidden: Boolean
        get() = false

    val inputItems: List<RecipeChoice>
    val inputFluids: List<PylonFluid> get() = emptyList()
    val outputItems: List<ItemStack>
    val outputFluids: List<PylonFluid> get() = emptyList()

    fun isInput(stack: ItemStack) = inputItems.any {
        if (it is RecipeChoice.MaterialChoice && PylonItem.fromStack(stack) != null) {
            return false
        }
        @Suppress("SENSELESS_COMPARISON") // I have found it's very easy to return null input item by accident, hence the seemingly redundant null check
        it != null && it.test(stack)
    }
    fun isInput(fluid: PylonFluid) = fluid in inputFluids
    fun isOutput(stack: ItemStack) = outputItems.any { stack.isPylonSimilar(it) }
    fun isOutput(fluid: PylonFluid) = fluid in outputFluids
    fun display(): Gui
}
