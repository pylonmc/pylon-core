package io.github.pylonmc.pylon.core.recipe

import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.item.base.VanillaCraftingItem
import io.github.pylonmc.pylon.core.item.base.VanillaSmithingMaterial
import io.github.pylonmc.pylon.core.item.base.VanillaSmithingTemplate
import io.github.pylonmc.pylon.core.pluginInstance
import org.bukkit.Bukkit
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockCookEvent
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.event.inventory.PrepareSmithingEvent
import org.bukkit.inventory.*

object RecipeTypes {

    @JvmField
    @Suppress("UNCHECKED_CAST")
    val VANILLA_BLASTING: RecipeType<BlastingRecipe> = CookingRecipeType("blasting") as RecipeType<BlastingRecipe>

    @JvmField
    @Suppress("UNCHECKED_CAST")
    val VANILLA_CAMPFIRE: RecipeType<CampfireRecipe> = CookingRecipeType("campfire") as RecipeType<CampfireRecipe>

    @JvmField
    val VANILLA_CRAFTING: RecipeType<CraftingRecipe> = CraftingRecipeType

    @JvmField
    @Suppress("UNCHECKED_CAST")
    val VANILLA_FURNACE: RecipeType<FurnaceRecipe> = CookingRecipeType("furnace") as RecipeType<FurnaceRecipe>

    @JvmField
    val VANILLA_SMITHING: RecipeType<SmithingRecipe> = SmithingRecipeType

    @JvmField
    @Suppress("UNCHECKED_CAST")
    val VANILLA_SMOKING: RecipeType<SmokingRecipe> = CookingRecipeType("smoking") as RecipeType<SmokingRecipe>

    init {
        VANILLA_BLASTING.register()
        VANILLA_CAMPFIRE.register()
        VANILLA_CRAFTING.register()
        VANILLA_FURNACE.register()
        VANILLA_SMITHING.register()
        VANILLA_SMOKING.register()
    }
}

private object CraftingRecipeType : VanillaRecipe<CraftingRecipe>("crafting") {

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onPreCraft(e: PrepareItemCraftEvent) {
        val recipe = e.recipe
        // All recipe types but MerchantRecipe implement Keyed
        if(recipe !is Keyed) return
        val inventory = e.inventory
        if (recipes.all { it.key != recipe.key } && inventory.any { it.isPylonAndIsNot<VanillaCraftingItem>() }) {
            // Prevent the erroneous crafting of vanilla items with Pylon ingredients
            inventory.result = null
        }
    }
}

private class CookingRecipeType(key: String) : VanillaRecipe<CookingRecipe<*>>(key) {

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onCook(e: BlockCookEvent) {
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

private object SmithingRecipeType : VanillaRecipe<SmithingRecipe>("smithing") {

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onSmith(e: PrepareSmithingEvent) {
        val inv = e.inventory
        val recipe = inv.recipe
        if(recipe !is Keyed) return
        if (
            recipes.all { it.key != recipe.key } &&
            (
                    inv.inputMineral.isPylonAndIsNot<VanillaSmithingMaterial>() ||
                    inv.inputTemplate.isPylonAndIsNot<VanillaSmithingTemplate>()
            )
        ) {
            // Prevent the erroneous smithing of vanilla items with Pylon ingredients
            inv.result = null
        }
    }
}

private abstract class VanillaRecipe<T>(key: String) : RecipeType<T>(
    NamespacedKey.minecraft(key)
), Listener where T : Keyed, T : Recipe {

    init {
        Bukkit.getPluginManager().registerEvents(this, pluginInstance)
    }

    override fun registerRecipe(recipe: T) {
        Bukkit.addRecipe(recipe)
    }

    override fun unregisterRecipe(recipe: NamespacedKey) {
        Bukkit.removeRecipe(recipe)
    }
}

private inline fun <reified T> ItemStack?.isPylonAndIsNot(): Boolean {
    val pylonItem = PylonItem.fromStack(this)
    return pylonItem != null && pylonItem !is T
}