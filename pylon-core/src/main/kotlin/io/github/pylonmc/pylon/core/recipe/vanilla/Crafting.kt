package io.github.pylonmc.pylon.core.recipe.vanilla

import io.github.pylonmc.pylon.core.guide.button.ItemButton
import io.github.pylonmc.pylon.core.util.gui.GuiItems
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.CraftingRecipe
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.RecipeChoice
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.Item


abstract class CraftingRecipeWrapper(val craftingRecipe: CraftingRecipe) : VanillaRecipeWrapper {
    override fun getKey(): NamespacedKey = craftingRecipe.key
    override fun getRecipe(): Recipe = craftingRecipe
    override fun getOutputItems(): List<ItemStack> = listOf(craftingRecipe.result)
}

class ShapedRecipeWrapper(val recipe: ShapedRecipe) : CraftingRecipeWrapper(recipe) {
    override fun getInputItems(): List<RecipeChoice> = recipe.choiceMap.values.filter { it != null }.toList()
    override fun display(): Gui {
        val gui = Gui.normal()
            .setStructure(
                "# # # # # # # # #",
                "# # # . . . # # #",
                "# b # . . . # r #",
                "# # # . . . # # #",
                "# # # # # # # # #",
            )
            .addIngredient('#', GuiItems.backgroundBlack())
            .addIngredient('b', ItemButton.fromStack(ItemStack(Material.CRAFTING_TABLE)))
            .addIngredient('r', ItemButton.fromStack(recipe.result))
            .build()

        val height = recipe.shape.size
        val width = recipe.shape[0].length
        for (x in 0..width-1) {
            for (y in 0..height-1) {
                gui.setItem(12 + x + 9 * y, getDisplaySlot(recipe, x, y))
            }
        }

        return gui
    }

    fun getDisplaySlot(recipe: ShapedRecipe, x: Int, y: Int): Item {
        val character = recipe.shape[y][x]
        return ItemButton.fromChoice(recipe.choiceMap[character])
    }
}

class ShapelessRecipeWrapper(val recipe: ShapelessRecipe) : CraftingRecipeWrapper(recipe) {
    override fun getInputItems(): List<RecipeChoice> = recipe.choiceList.filter { it != null }
    override fun display() = Gui.normal()
            .setStructure(
                "# # # # # # # # #",
                "# # # 0 1 2 # # #",
                "# b # 3 4 5 # r #",
                "# # # 6 7 8 # # #",
                "# # # # # # # # #",
            )
            .addIngredient('#', GuiItems.backgroundBlack())
            .addIngredient('b', ItemButton.fromStack(ItemStack(Material.CRAFTING_TABLE)))
            .addIngredient('0', getDisplaySlot(recipe, 0))
            .addIngredient('1', getDisplaySlot(recipe, 1))
            .addIngredient('2', getDisplaySlot(recipe, 2))
            .addIngredient('3', getDisplaySlot(recipe, 3))
            .addIngredient('4', getDisplaySlot(recipe, 4))
            .addIngredient('5', getDisplaySlot(recipe, 5))
            .addIngredient('6', getDisplaySlot(recipe, 6))
            .addIngredient('7', getDisplaySlot(recipe, 7))
            .addIngredient('8', getDisplaySlot(recipe, 8))
            .addIngredient('r', recipe.result)
            .build()

    fun getDisplaySlot(recipe: ShapelessRecipe, index: Int): Item {
        return ItemButton.fromChoice(recipe.choiceList.getOrNull(index))
    }
}

object ShapedRecipeType : VanillaRecipeType<ShapedRecipeWrapper>("shaped") {
    fun addRecipe(recipe: ShapedRecipe) = super.addRecipe(ShapedRecipeWrapper(recipe))
}

object ShapelessRecipeType : VanillaRecipeType<ShapelessRecipeWrapper>("shapeless") {
    fun addRecipe(recipe: ShapelessRecipe) = super.addRecipe(ShapelessRecipeWrapper(recipe))
}
