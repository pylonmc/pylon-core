package io.github.pylonmc.pylon.core.recipe.vanilla

import io.github.pylonmc.pylon.core.config.ConfigSection
import io.github.pylonmc.pylon.core.guide.button.ItemButton
import io.github.pylonmc.pylon.core.recipe.FluidOrItem
import io.github.pylonmc.pylon.core.util.gui.GuiItems
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.*
import org.bukkit.inventory.meta.trim.TrimPattern
import xyz.xenondevs.invui.gui.Gui


sealed class SmithingRecipeWrapper(recipe: SmithingRecipe) : VanillaRecipeWrapper {

    abstract override val recipe: SmithingRecipe
    override val inputs: List<FluidOrItem> = FluidOrItem.of(recipe.base) + FluidOrItem.of(recipe.addition)
    override val results: List<FluidOrItem> = listOf(FluidOrItem.of(recipe.result))

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
        .addIngredient('1', ItemButton.fromChoice(recipe.base))
        .addIngredient('2', ItemButton.fromChoice(recipe.addition))
        .addIngredient('r', ItemButton.fromStack(recipe.result))
        .build()

    override fun getKey(): NamespacedKey = recipe.key
}

class SmithingTransformRecipeWrapper(override val recipe: SmithingTransformRecipe) : SmithingRecipeWrapper(recipe)
class SmithingTrimRecipeWrapper(override val recipe: SmithingTrimRecipe) : SmithingRecipeWrapper(recipe)

/**
 * Key: `minecraft:smithing_transform`
 */
object SmithingTransformRecipeType : VanillaRecipeType<SmithingTransformRecipeWrapper>(
    "smithing_transform",
    SmithingTransformRecipeWrapper::class.java
) {

    fun addRecipe(recipe: SmithingTransformRecipe) = super.addRecipe(SmithingTransformRecipeWrapper(recipe))

    override fun loadRecipe(key: NamespacedKey, section: ConfigSection): SmithingTransformRecipeWrapper {
        val template = convertTypeOrThrow<ItemStack>(section.getOrThrow("template"))
        val base = convertTypeOrThrow<ItemStack>(section.getOrThrow("base"))
        val addition = convertTypeOrThrow<ItemStack>(section.getOrThrow("addition"))
        val result = convertTypeOrThrow<ItemStack>(section.getOrThrow("result"))
        return SmithingTransformRecipeWrapper(
            SmithingTransformRecipe(
                key,
                result,
                RecipeChoice.ExactChoice(template),
                RecipeChoice.ExactChoice(base),
                RecipeChoice.ExactChoice(addition)
            )
        )
    }
}

/**
 * Key: `minecraft:smithing_trim`
 */
object SmithingTrimRecipeType : VanillaRecipeType<SmithingTrimRecipeWrapper>("smithing_trim", SmithingTrimRecipeWrapper::class.java) {

    fun addRecipe(recipe: SmithingTrimRecipe) = super.addRecipe(SmithingTrimRecipeWrapper(recipe))

    override fun loadRecipe(key: NamespacedKey, section: ConfigSection): SmithingTrimRecipeWrapper {
        val pattern = convertTypeOrThrow<TrimPattern>(section.getOrThrow("pattern"))
        val template = convertTypeOrThrow<ItemStack>(section.getOrThrow("template"))
        val base = convertTypeOrThrow<ItemStack>(section.getOrThrow("base"))
        val addition = convertTypeOrThrow<ItemStack>(section.getOrThrow("addition"))
        return SmithingTrimRecipeWrapper(
            SmithingTrimRecipe(
                key,
                RecipeChoice.ExactChoice(template),
                RecipeChoice.ExactChoice(base),
                RecipeChoice.ExactChoice(addition),
                pattern
            )
        )
    }
}
