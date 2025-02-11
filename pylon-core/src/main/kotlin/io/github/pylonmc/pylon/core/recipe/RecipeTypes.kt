package io.github.pylonmc.pylon.core.recipe

import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.Bukkit
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.inventory.*

object RecipeTypes {

    @JvmField
    val VANILLA_BLASTING = vanillaRecipeWrapper<BlastingRecipe>("vanilla_blasting")

    @JvmField
    val VANILLA_CAMPFIRE = vanillaRecipeWrapper<CampfireRecipe>("vanilla_campfire")

    @JvmField
    val VANILLA_CRAFTING = vanillaRecipeWrapper<CraftingRecipe>("vanilla_crafting")

    @JvmField
    val VANILLA_FURNACE = vanillaRecipeWrapper<FurnaceRecipe>("vanilla_furnace")

    @JvmField
    val VANILLA_SMITHING = vanillaRecipeWrapper<SmithingRecipe>("vanilla_smithing")

    @JvmField
    val VANILLA_SMOKING = vanillaRecipeWrapper<SmokingRecipe>("vanilla_smoking")

    @JvmField
    val VANILLA_STONECUTTING = vanillaRecipeWrapper<StonecuttingRecipe>("vanilla_stonecutting")

    @JvmField
    val MOB_DROP: RecipeType<MobDropRecipe> = MobDropRecipeType
}

private fun <T> vanillaRecipeWrapper(keyStr: String): RecipeType<T>
        where T : Keyed, T : Recipe {
    val key = pylonKey(keyStr)
    return object : RecipeType<T>() {

        override fun addRecipe(recipe: T) {
            super.addRecipe(recipe)
            Bukkit.addRecipe(recipe)
        }

        override fun removeRecipe(recipe: NamespacedKey) {
            super.removeRecipe(recipe)
            Bukkit.removeRecipe(recipe)
        }

        override fun getKey(): NamespacedKey = key
    }.also { it.register() }
}