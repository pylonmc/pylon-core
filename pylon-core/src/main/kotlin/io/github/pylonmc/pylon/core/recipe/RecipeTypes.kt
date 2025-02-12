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
    return object : RecipeType<T>(pylonKey(keyStr)) {

        override fun registerRecipe(recipe: T) {
            Bukkit.addRecipe(recipe)
        }

        override fun unregisterRecipe(recipe: NamespacedKey) {
            Bukkit.removeRecipe(recipe)
        }
    }.also { it.register() }
}