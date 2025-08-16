package io.github.pylonmc.pylon.core.recipe.vanilla

import io.github.pylonmc.pylon.core.guide.button.ItemButton
import io.github.pylonmc.pylon.core.recipe.FluidOrItem
import io.github.pylonmc.pylon.core.util.gui.GuiItems
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.SmithingRecipe
import xyz.xenondevs.invui.gui.Gui


class SmithingRecipeWrapper(val smithingRecipe: SmithingRecipe) : VanillaRecipeWrapper {

    override val recipe: Recipe = smithingRecipe
    override val inputs: List<FluidOrItem> = FluidOrItem.of(smithingRecipe.base) + FluidOrItem.of(smithingRecipe.addition)
    override val results: List<FluidOrItem> = listOf(FluidOrItem.of(smithingRecipe.result))

    override fun display() = Gui.normal()
        .setStructure(
            "# # # # # # # # #",
            "# # # # # # # # #",
            "# b # 0 1 2 # r #",
            "# # # # # # # # #",
            "# # # # # # # # #",
        )
        .addIngredient('#', GuiItems.backgroundBlack())
        .addIngredient('b', ItemButton.fromStack(ItemStack(Material.SMITHING_TABLE)))
        .addIngredient('0', ItemStack(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE))
        .addIngredient('1', ItemButton.fromChoice(smithingRecipe.base))
        .addIngredient('2', ItemButton.fromChoice(smithingRecipe.addition))
        .addIngredient('r', ItemButton.fromStack(smithingRecipe.result))
        .build()

    override fun getKey(): NamespacedKey = smithingRecipe.key
}

object SmithingTransformRecipeType : VanillaRecipeType<SmithingRecipeWrapper>("smithing_transform", SmithingRecipeWrapper::class.java) {
    fun addRecipe(recipe: SmithingRecipe) = super.addRecipe(SmithingRecipeWrapper(recipe))
}

object SmithingTrimRecipeType : VanillaRecipeType<SmithingRecipeWrapper>("smithing_trim", SmithingRecipeWrapper::class.java) {
    fun addRecipe(recipe: SmithingRecipe) = super.addRecipe(SmithingRecipeWrapper(recipe))
}
