package io.github.pylonmc.pylon.core.recipe

import io.github.pylonmc.pylon.core.fluid.PylonFluid
import org.bukkit.Keyed
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.gui.Gui

/**
 * A sort of "pseudo recipe" that does not correspond to any real recipe, but is used for displaying
 * stuff in the guide. For example, the water pump uses a `PylonVirtualRecipe` to display the
 * output of the pump in the guide, even though it's not a "recipe" per se.
 *
 * @see [io.github.pylonmc.pylon.core.content.guide.PylonGuide.addVirtualRecipe]
 */
interface PylonVirtualRecipe : Keyed {

    fun isInput(stack: ItemStack): Boolean
    fun isInput(fluid: PylonFluid): Boolean
    fun isOutput(stack: ItemStack): Boolean
    fun isOutput(fluid: PylonFluid): Boolean

    fun display(): Gui
}