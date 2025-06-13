package io.github.pylonmc.pylon.core.recipe

import io.github.pylonmc.pylon.core.fluid.PylonFluid
import org.bukkit.Keyed
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.gui.Gui

interface PylonRecipe : Keyed {
    fun getInputItems(): Set<ItemStack> = TODO()

    fun getInputFluids(): Set<PylonFluid> = TODO()

    fun getOutputItems(): Set<ItemStack> = TODO()

    fun getOutputFluids(): Set<PylonFluid> = TODO()

    fun display(): Gui = TODO()
}