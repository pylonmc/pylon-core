package io.github.pylonmc.pylon.core.recipe.vanilla

import io.github.pylonmc.pylon.core.guide.button.ItemButton
import io.github.pylonmc.pylon.core.i18n.PylonArgument
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.recipe.FluidOrItem
import io.github.pylonmc.pylon.core.util.gui.GuiItems
import io.github.pylonmc.pylon.core.util.gui.unit.UnitFormat
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.*
import xyz.xenondevs.invui.gui.AbstractGui
import xyz.xenondevs.invui.gui.Gui


sealed class CookingRecipeWrapper(final override val recipe: CookingRecipe<*>) : VanillaRecipeWrapper {
    override val inputs: List<FluidOrItem> = FluidOrItem.of(recipe.inputChoice)
    override val results: List<FluidOrItem> = listOf(FluidOrItem.of(recipe.result))
    override fun getKey(): NamespacedKey = recipe.key
}

class BlastingRecipeWrapper(recipe: BlastingRecipe) : CookingRecipeWrapper(recipe) {
    override fun display() = display(recipe, Material.BLAST_FURNACE)
}

class CampfireRecipeWrapper(recipe: CampfireRecipe) : CookingRecipeWrapper(recipe) {
    override fun display() = display(recipe, Material.BLAST_FURNACE)
}

class FurnaceRecipeWrapper(recipe: FurnaceRecipe) : CookingRecipeWrapper(recipe) {
    override fun display() = display(recipe, Material.FURNACE)
}

class SmokingRecipeWrapper(recipe: SmokingRecipe) : CookingRecipeWrapper(recipe) {
    override fun display() = display(recipe, Material.CAMPFIRE)
}

private fun display(recipe: CookingRecipe<*>, block: Material) = Gui.normal()
        .setStructure(
            "# # # # # # # # #",
            "# # # # # # # # #",
            "# b # # i f o # #",
            "# # # # # # # # #",
            "# # # # # # # # #",
        )
        .addIngredient('#', GuiItems.backgroundBlack())
        .addIngredient('b', ItemStack(block))
        .addIngredient('i', ItemButton.fromChoice(recipe.inputChoice))
        .addIngredient('f', GuiItems.progressCyclingItem(recipe.cookingTime,
            ItemStackBuilder.of(Material.COAL)
                .name(Component.translatable(
                    "pylon.pyloncore.guide.recipe.cooking",
                    PylonArgument.of("time", UnitFormat.SECONDS.format(recipe.cookingTime / 20))
                ))))
        .addIngredient('o', ItemButton.fromStack(recipe.result))
        .build() as AbstractGui

/**
 * Key: `minecraft:blasting`
 */
object BlastingRecipeType : VanillaRecipeType<BlastingRecipeWrapper>("blasting", BlastingRecipeWrapper::class.java) {
    fun addRecipe(recipe: BlastingRecipe) = super.addRecipe(BlastingRecipeWrapper(recipe))
}

/**
 * Key: `minecraft:campfire_cooking`
 */
object CampfireRecipeType : VanillaRecipeType<CampfireRecipeWrapper>("campfire_cooking", CampfireRecipeWrapper::class.java) {
    fun addRecipe(recipe: CampfireRecipe) = super.addRecipe(CampfireRecipeWrapper(recipe))
}

/**
 * Key: `minecraft:smelting`
 */
object FurnaceRecipeType : VanillaRecipeType<FurnaceRecipeWrapper>("smelting", FurnaceRecipeWrapper::class.java) {
    fun addRecipe(recipe: FurnaceRecipe) = super.addRecipe(FurnaceRecipeWrapper(recipe))
}

/**
 * Key: `minecraft:smoking`
 */
object SmokingRecipeType : VanillaRecipeType<SmokingRecipeWrapper>("smoking", SmokingRecipeWrapper::class.java) {
    fun addRecipe(recipe: SmokingRecipe) = super.addRecipe(SmokingRecipeWrapper(recipe))
}
