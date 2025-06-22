package io.github.pylonmc.pylon.core.recipe.vanilla

import io.github.pylonmc.pylon.core.guide.button.ItemButton
import io.github.pylonmc.pylon.core.util.gui.GuiItems
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.RecipeChoice
import org.bukkit.inventory.SmithingRecipe
import xyz.xenondevs.invui.gui.Gui


class SmithingRecipeWrapper(val smithingRecipe: SmithingRecipe) : VanillaRecipeWrapper {
    override fun getKey(): NamespacedKey = smithingRecipe.key
    override fun getRecipe(): Recipe = smithingRecipe
    override fun getInputItems(): List<RecipeChoice> = listOf(smithingRecipe.base, smithingRecipe.addition)
    override fun getOutputItems(): List<ItemStack> = listOf(smithingRecipe.result)
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
}

object SmithingRecipeType : VanillaRecipeType<SmithingRecipeWrapper>("smithing") {
    fun addRecipe(recipe: SmithingRecipe) = super.addRecipe(SmithingRecipeWrapper(recipe))
}
