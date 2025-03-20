package io.github.pylonmc.pylon.core.recipe

import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.pluginInstance
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.Bukkit
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.FurnaceSmeltEvent
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.inventory.*

object RecipeTypes {

    @JvmField
    @Suppress("UNCHECKED_CAST")
    val VANILLA_BLASTING: RecipeType<BlastingRecipe> = FurnaceRecipeType("blasting") as RecipeType<BlastingRecipe>

    @JvmField
    @Suppress("UNCHECKED_CAST")
    val VANILLA_CAMPFIRE: RecipeType<CampfireRecipe> = FurnaceRecipeType("campfire") as RecipeType<CampfireRecipe>

    @JvmField
    val VANILLA_CRAFTING: RecipeType<CraftingRecipe> = CraftingRecipeType

    @JvmField
    @Suppress("UNCHECKED_CAST")
    val VANILLA_FURNACE: RecipeType<FurnaceRecipe> = FurnaceRecipeType("furnace") as RecipeType<FurnaceRecipe>

    @JvmField
    val VANILLA_SMITHING = vanillaRecipeWrapper<SmithingRecipe>("vanilla_smithing")

    @JvmField
    @Suppress("UNCHECKED_CAST")
    val VANILLA_SMOKING: RecipeType<SmokingRecipe> = FurnaceRecipeType("smoking") as RecipeType<SmokingRecipe>
}

private object CraftingRecipeType : RecipeType<CraftingRecipe>(
    NamespacedKey.minecraft("crafting")
), Listener {

    init {
        Bukkit.getPluginManager().registerEvents(this, pluginInstance)
    }

    override fun registerRecipe(recipe: CraftingRecipe) {
        Bukkit.addRecipe(recipe)
    }

    override fun unregisterRecipe(recipe: NamespacedKey) {
        Bukkit.removeRecipe(recipe)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onPreCraft(e: PrepareItemCraftEvent) {
        val recipe = e.recipe
        val inventory = e.inventory
        if (recipes.all { it.key != recipe } && inventory.any { PylonItem.fromStack(it) != null }) {
            // Prevent the erroneous crafting of vanilla items with Pylon ingredients
            inventory.result = null
        }
    }
}

private class FurnaceRecipeType(key: String) : RecipeType<CookingRecipe<*>>(
    NamespacedKey.minecraft(key)
), Listener {

    init {
        Bukkit.getPluginManager().registerEvents(this, pluginInstance)
    }

    override fun registerRecipe(recipe: CookingRecipe<*>) {
        Bukkit.addRecipe(recipe)
    }

    override fun unregisterRecipe(recipe: NamespacedKey) {
        Bukkit.removeRecipe(recipe)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onFurnaceCook(e: FurnaceSmeltEvent) {
        val input = e.source
        if (PylonItem.fromStack(input) == null) return
        for (recipe in recipes) {
            if (recipe.inputChoice.test(input)) {
                e.result = recipe.result.clone()
                return
            }
        }
    }
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