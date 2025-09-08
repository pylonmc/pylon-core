package io.github.pylonmc.pylon.core.recipe.vanilla

import io.github.pylonmc.pylon.core.config.ConfigSection
import io.github.pylonmc.pylon.core.config.adapter.ConfigAdapter
import io.github.pylonmc.pylon.core.guide.button.ItemButton
import io.github.pylonmc.pylon.core.recipe.FluidOrItem
import io.github.pylonmc.pylon.core.recipe.RecipeInput
import io.github.pylonmc.pylon.core.util.gui.GuiItems
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.*
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.Item


sealed class CraftingRecipeWrapper(val craftingRecipe: CraftingRecipe) : VanillaRecipeWrapper {
    override fun getKey(): NamespacedKey = craftingRecipe.key
    override val results: List<FluidOrItem> = listOf(FluidOrItem.of(craftingRecipe.result))
}

class ShapedRecipeWrapper(override val recipe: ShapedRecipe) : CraftingRecipeWrapper(recipe) {
    override val inputs: List<RecipeInput> =
        recipe.shape
            .flatMap { it.asIterable() }
            .mapNotNull {
                recipe.choiceMap[it]?.asRecipeInput()
            }

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

    private fun getDisplaySlot(recipe: ShapedRecipe, x: Int, y: Int): Item {
        val character = recipe.shape[y][x]
        return ItemButton.fromChoice(recipe.choiceMap[character])
    }
}

sealed class AShapelessRecipeWrapper(recipe: CraftingRecipe) : CraftingRecipeWrapper(recipe) {

    protected abstract val choiceList: List<RecipeChoice?>

    override val inputs: List<RecipeInput> by lazy { choiceList.filterNotNull().map(RecipeChoice::asRecipeInput) }

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

/**
 * Key: `minecraft:crafting_shaped`
 */
object ShapedRecipeType : VanillaRecipeType<ShapedRecipeWrapper>("crafting_shaped") {

    fun addRecipe(recipe: ShapedRecipe) = super.addRecipe(ShapedRecipeWrapper(recipe))

    override fun loadRecipe(key: NamespacedKey, section: ConfigSection): ShapedRecipeWrapper {
        val ingredientKey = section.getOrThrow("key", ConfigAdapter.MAP.from(ConfigAdapter.CHAR, ConfigAdapter.ITEM_STACK))
        val pattern = section.getOrThrow("pattern", ConfigAdapter.LIST.from(ConfigAdapter.STRING))
        val result = section.getOrThrow("result", ConfigAdapter.ITEM_STACK)

        val recipe = ShapedRecipe(key, result)
        recipe.shape(*pattern.toTypedArray())
        for ((character, itemStack) in ingredientKey) {
            recipe.setIngredient(character, RecipeChoice.ExactChoice(itemStack))
        }
        return ShapedRecipeWrapper(recipe)
    }
}

/**
 * Key: `minecraft:crafting_shapeless`
 */
object ShapelessRecipeType : VanillaRecipeType<ShapelessRecipeWrapper>("crafting_shapeless") {

    fun addRecipe(recipe: ShapelessRecipe) = super.addRecipe(ShapelessRecipeWrapper(recipe))

    override fun loadRecipe(key: NamespacedKey, section: ConfigSection): ShapelessRecipeWrapper {
        val ingredients = section.getOrThrow("ingredients", ConfigAdapter.LIST.from(ConfigAdapter.ITEM_STACK))
        val result = section.getOrThrow("result", ConfigAdapter.ITEM_STACK)

        val recipe = ShapelessRecipe(key, result)
        for (ingredient in ingredients) {
            recipe.addIngredient(RecipeChoice.ExactChoice(ingredient))
        }
        return ShapelessRecipeWrapper(recipe)
    }
}

/**
 * Key: `minecraft:crafting_transmute`
 */
object TransmuteRecipeType : VanillaRecipeType<TransmuteRecipeWrapper>("crafting_transmute") {

    fun addRecipe(recipe: TransmuteRecipe) = super.addRecipe(TransmuteRecipeWrapper(recipe))

    override fun loadRecipe(key: NamespacedKey, section: ConfigSection): TransmuteRecipeWrapper {
        val input = section.getOrThrow("input", ConfigAdapter.ITEM_STACK)
        val material = section.getOrThrow("material", ConfigAdapter.ITEM_STACK)
        val result = section.getOrThrow("result", ConfigAdapter.MATERIAL)
        val recipe = TransmuteRecipe(key, result, RecipeChoice.ExactChoice(input), RecipeChoice.ExactChoice(material))
        return TransmuteRecipeWrapper(recipe)
    }
}