package io.github.pylonmc.pylon.core.recipe.vanilla

import io.github.pylonmc.pylon.core.guide.button.ItemButton
import io.github.pylonmc.pylon.core.recipe.FluidOrItem
import io.github.pylonmc.pylon.core.util.gui.GuiItems
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.*
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.Item


abstract class CraftingRecipeWrapper(val craftingRecipe: CraftingRecipe) : VanillaRecipeWrapper {
    override fun getKey(): NamespacedKey = craftingRecipe.key
    override val results: List<FluidOrItem> = listOf(FluidOrItem.of(craftingRecipe.result))
}

class ShapedRecipeWrapper(override val recipe: ShapedRecipe) : CraftingRecipeWrapper(recipe) {
    override val inputs: List<FluidOrItem> = recipe.choiceMap.values.filterNotNull().flatMap(FluidOrItem::of)

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
        for (x in 0 until width) {
            for (y in 0 until height) {
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

abstract class AShapelessRecipeWrapper(recipe: CraftingRecipe) : CraftingRecipeWrapper(recipe) {

    protected abstract val choiceList: List<RecipeChoice?>

    override val inputs = choiceList.filterNotNull().flatMap(FluidOrItem::of)

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
        .addIngredient('0', getDisplaySlot(0))
        .addIngredient('1', getDisplaySlot(1))
        .addIngredient('2', getDisplaySlot(2))
        .addIngredient('3', getDisplaySlot(3))
        .addIngredient('4', getDisplaySlot(4))
        .addIngredient('5', getDisplaySlot(5))
        .addIngredient('6', getDisplaySlot(6))
        .addIngredient('7', getDisplaySlot(7))
        .addIngredient('8', getDisplaySlot(8))
        .addIngredient('r', recipe.result)
        .build()

    private fun getDisplaySlot(index: Int): Item {
        return ItemButton.fromChoice(choiceList.getOrNull(index))
    }
}

class ShapelessRecipeWrapper(override val recipe: ShapelessRecipe) : AShapelessRecipeWrapper(recipe) {
    override val choiceList = recipe.choiceList
}

class TransmuteRecipeWrapper(override val recipe: TransmuteRecipe) : AShapelessRecipeWrapper(recipe) {
    override val choiceList = listOf(recipe.input, recipe.material)
}

object ShapedRecipeType : VanillaRecipeType<ShapedRecipeWrapper>("crafting_shaped", ShapedRecipeWrapper::class.java) {
    fun addRecipe(recipe: ShapedRecipe) = super.addRecipe(ShapedRecipeWrapper(recipe))
}

object ShapelessRecipeType : VanillaRecipeType<ShapelessRecipeWrapper>("crafting_shapeless", ShapelessRecipeWrapper::class.java) {
    fun addRecipe(recipe: ShapelessRecipe) = super.addRecipe(ShapelessRecipeWrapper(recipe))
}

object TransmuteRecipeType : VanillaRecipeType<TransmuteRecipeWrapper>("crafting_transmute", TransmuteRecipeWrapper::class.java) {
    fun addRecipe(recipe: TransmuteRecipe) = super.addRecipe(TransmuteRecipeWrapper(recipe))
}
