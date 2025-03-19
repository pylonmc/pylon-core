package io.github.pylonmc.pylon.core.recipe

import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.item.PylonItemSchema
import io.github.pylonmc.pylon.core.pluginInstance
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.Bukkit
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.inventory.*

object RecipeTypes {

    @JvmField
    val VANILLA_BLASTING = vanillaRecipeWrapper<BlastingRecipe>("vanilla_blasting")

    @JvmField
    val VANILLA_CAMPFIRE = vanillaRecipeWrapper<CampfireRecipe>("vanilla_campfire")

    @JvmField
    val VANILLA_CRAFTING: RecipeType<CraftingRecipe> = CraftingRecipeType

    @JvmField
    val VANILLA_FURNACE = vanillaRecipeWrapper<FurnaceRecipe>("vanilla_furnace")

    @JvmField
    val VANILLA_SMITHING = vanillaRecipeWrapper<SmithingRecipe>("vanilla_smithing")

    @JvmField
    val VANILLA_SMOKING = vanillaRecipeWrapper<SmokingRecipe>("vanilla_smoking")

    @JvmField
    val VANILLA_STONECUTTING = vanillaRecipeWrapper<StonecuttingRecipe>("vanilla_stonecutting")
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