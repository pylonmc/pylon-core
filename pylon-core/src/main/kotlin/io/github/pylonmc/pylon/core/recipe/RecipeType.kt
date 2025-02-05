package io.github.pylonmc.pylon.core.recipe

import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.Bukkit
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.inventory.*

interface RecipeType<T> : Keyed {

    fun addRecipe(recipe: T)

    fun register() {
        PylonRegistry.RECIPE_TYPES.register(this)
    }

    companion object {

        @JvmField
        val VANILLA_BLASTING = vanillaRecipeWrapper<BlastingRecipe>("vanilla_blasting")

        @JvmField
        val VANILLA_CAMPFIRE = vanillaRecipeWrapper<CampfireRecipe>("vanilla_campfire")

        @JvmField
        val VANILLA_CRAFTING_TABLE = vanillaRecipeWrapper<CraftingRecipe>("vanilla_crafting")

        @JvmField
        val VANILLA_FURNACE = vanillaRecipeWrapper<FurnaceRecipe>("vanilla_furnace")

        @JvmField
        val VANILLA_SMITHING_TABLE = vanillaRecipeWrapper<SmithingRecipe>("vanilla_smithing")

        @JvmField
        val VANILLA_SMOKING = vanillaRecipeWrapper<SmokingRecipe>("vanilla_smoking")

        @JvmField
        val VANILLA_STONECUTTING = vanillaRecipeWrapper<StonecuttingRecipe>("vanilla_stonecutting")

        @JvmField
        val MOB_DROP: RecipeType<MobDropRecipe> = MobDropRecipeType
    }
}

private fun <T : Recipe> vanillaRecipeWrapper(keyStr: String): RecipeType<T> {
    val key = pylonKey(keyStr)
    return object : RecipeType<T> {
        override fun addRecipe(recipe: T) {
            Bukkit.addRecipe(recipe)
        }

        override fun getKey(): NamespacedKey = key
    }.also { it.register() }
}