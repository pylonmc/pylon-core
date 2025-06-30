package io.github.pylonmc.pylon.core.recipe

import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.item.base.VanillaCraftingItem
import io.github.pylonmc.pylon.core.item.base.VanillaSmithingMaterial
import io.github.pylonmc.pylon.core.item.base.VanillaSmithingTemplate
import io.github.pylonmc.pylon.core.util.isPylonAndIsNot
import org.bukkit.Bukkit
import org.bukkit.Keyed
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockCookEvent
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.event.inventory.PrepareSmithingEvent

internal object PylonRecipeListener : Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onPreCraft(e: PrepareItemCraftEvent) {
        val recipe = e.recipe
        // All recipe types but MerchantRecipe implement Keyed
        if (recipe !is Keyed) return
        val inventory = e.inventory

        val hasPylonItems = inventory.any { it.isPylonAndIsNot<VanillaCraftingItem>() }
        val isNotPylonCraftingRecipe = RecipeType.vanillaCraftingRecipes().all {
            it.key != recipe.key
        }

        if (hasPylonItems && isNotPylonCraftingRecipe) {
            // Prevent the erroneous crafting of vanilla items with Pylon ingredients
            inventory.result = null
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onCook(e: BlockCookEvent) {
        val input = e.source
        if (PylonItem.fromStack(input) == null) return

        for (recipe in RecipeType.vanillaCookingRecipes()) {
            if (recipe.recipe.inputChoice.test(input)) {
                e.result = recipe.recipe.result.clone()
                return
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onSmith(e: PrepareSmithingEvent) {
        val inv = e.inventory
        val recipe = inv.recipe
        if (recipe !is Keyed) return
        if (
            RecipeType.VANILLA_SMITHING.all { it.key != recipe.key } &&
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