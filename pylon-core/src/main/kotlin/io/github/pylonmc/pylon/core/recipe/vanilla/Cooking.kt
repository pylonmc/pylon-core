package io.github.pylonmc.pylon.core.recipe.vanilla

import io.github.pylonmc.pylon.core.config.ConfigSection
import io.github.pylonmc.pylon.core.config.adapter.ConfigAdapter
import io.github.pylonmc.pylon.core.guide.button.ItemButton
import io.github.pylonmc.pylon.core.i18n.PylonArgument
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.recipe.FluidOrItem
import io.github.pylonmc.pylon.core.recipe.RecipeInput
import io.github.pylonmc.pylon.core.util.gui.GuiItems
import io.github.pylonmc.pylon.core.util.gui.unit.UnitFormat
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.*
import org.bukkit.inventory.recipe.CookingBookCategory
import xyz.xenondevs.invui.gui.Gui


sealed class CookingRecipeWrapper(final override val recipe: CookingRecipe<*>) : VanillaRecipeWrapper {
    override val inputs: List<RecipeInput> = listOf(recipe.inputChoice.asRecipeInput())
    override val results: List<FluidOrItem> = listOf(FluidOrItem.of(recipe.result))
    override fun getKey(): NamespacedKey = recipe.key

    protected abstract val displayBlock: Material

    override fun display(): Gui = Gui.normal()
        .setStructure(
            "# # # # # # # # #",
            "# # # # # # # # #",
            "# b # # i f o # #",
            "# # # # # # # # #",
            "# # # # # # # # #",
        )
        .addIngredient('#', GuiItems.backgroundBlack())
        .addIngredient('b', ItemStack(displayBlock))
        .addIngredient('i', ItemButton.fromChoice(recipe.inputChoice))
        .addIngredient(
            'f', GuiItems.progressCyclingItem(
                recipe.cookingTime,
                ItemStackBuilder.of(Material.COAL)
                    .name(
                        Component.translatable(
                            "pylon.pyloncore.guide.recipe.cooking",
                            PylonArgument.of("time", UnitFormat.SECONDS.format(recipe.cookingTime / 20))
                        )
                    )
            )
        )
        .addIngredient('o', ItemButton.fromStack(recipe.result))
        .build()
}

class BlastingRecipeWrapper(recipe: BlastingRecipe) : CookingRecipeWrapper(recipe) {
    override val displayBlock = Material.BLAST_FURNACE
}

class CampfireRecipeWrapper(recipe: CampfireRecipe) : CookingRecipeWrapper(recipe) {
    override val displayBlock = Material.CAMPFIRE
}

class FurnaceRecipeWrapper(recipe: FurnaceRecipe) : CookingRecipeWrapper(recipe) {
    override val displayBlock = Material.FURNACE
}

class SmokingRecipeWrapper(recipe: SmokingRecipe) : CookingRecipeWrapper(recipe) {
    override val displayBlock = Material.SMOKER
}

private inline fun <T : CookingRecipe<T>> loadCookingRecipe(
    key: NamespacedKey,
    config: ConfigSection,
    cookingTime: Int,
    cons: (NamespacedKey, ItemStack, RecipeChoice, Float, Int) -> T
): T {
    val cookingTime = config.get("cookingtime", ConfigAdapter.INT, cookingTime)
    val experience = config.get("experience", ConfigAdapter.FLOAT, 0f)
    val ingredient = config.getOrThrow("ingredient", ConfigAdapter.ITEM_STACK)
    val result = config.getOrThrow("result", ConfigAdapter.ITEM_STACK)
    val category = config.get("category", ConfigAdapter.ENUM.from(), CookingBookCategory.FOOD)
    val group = config.get("group", ConfigAdapter.STRING, "")
    val recipe = cons(key, result, RecipeChoice.ExactChoice(ingredient), experience, cookingTime)
    recipe.category = category
    recipe.group = group
    return recipe
}

/**
 * Key: `minecraft:blasting`
 */
object BlastingRecipeType : VanillaRecipeType<BlastingRecipeWrapper>("blasting") {

    fun addRecipe(recipe: BlastingRecipe) = super.addRecipe(BlastingRecipeWrapper(recipe))

    override fun loadRecipe(key: NamespacedKey, section: ConfigSection) =
        BlastingRecipeWrapper(loadCookingRecipe(key, section, 100, ::BlastingRecipe))
}

/**
 * Key: `minecraft:campfire_cooking`
 *
 * Despite the vanilla default cooking time being 100 ticks, we set it to 600 ticks here
 * to match the actual in-game behavior
 */
object CampfireRecipeType : VanillaRecipeType<CampfireRecipeWrapper>("campfire_cooking") {

    fun addRecipe(recipe: CampfireRecipe) = super.addRecipe(CampfireRecipeWrapper(recipe))

    override fun loadRecipe(key: NamespacedKey, section: ConfigSection) =
        CampfireRecipeWrapper(loadCookingRecipe(key, section, 600, ::CampfireRecipe))
}

/**
 * Key: `minecraft:smelting`
 */
object FurnaceRecipeType : VanillaRecipeType<FurnaceRecipeWrapper>("smelting") {

    fun addRecipe(recipe: FurnaceRecipe) = super.addRecipe(FurnaceRecipeWrapper(recipe))

    override fun loadRecipe(key: NamespacedKey, section: ConfigSection) =
        FurnaceRecipeWrapper(loadCookingRecipe(key, section, 200, ::FurnaceRecipe))
}

/**
 * Key: `minecraft:smoking`
 */
object SmokingRecipeType : VanillaRecipeType<SmokingRecipeWrapper>("smoking") {

    fun addRecipe(recipe: SmokingRecipe) = super.addRecipe(SmokingRecipeWrapper(recipe))

    override fun loadRecipe(key: NamespacedKey, section: ConfigSection) =
        SmokingRecipeWrapper(loadCookingRecipe(key, section, 100, ::SmokingRecipe))
}
