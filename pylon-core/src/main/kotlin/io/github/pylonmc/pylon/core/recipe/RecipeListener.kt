package io.github.pylonmc.pylon.core.recipe

import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.item.base.*
import io.github.pylonmc.pylon.core.item.research.Research.Companion.canCraft
import io.github.pylonmc.pylon.core.util.isPylonAndIsNot
import org.bukkit.Keyed
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockCookEvent
import org.bukkit.event.inventory.FurnaceBurnEvent
import org.bukkit.event.inventory.FurnaceSmeltEvent
import org.bukkit.event.inventory.FurnaceStartSmeltEvent
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.event.inventory.PrepareSmithingEvent
import org.bukkit.inventory.CookingRecipe
import org.bukkit.inventory.ItemStack

internal object PylonRecipeListener : Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onPreCraft(e: PrepareItemCraftEvent) {
        val recipe = e.recipe
        // All recipe types but MerchantRecipe implement Keyed
        if (recipe !is Keyed) return
        val inventory = e.inventory

        val hasPylonItems = inventory.any { it.isPylonAndIsNot<VanillaCraftingItem>() }
        if (hasPylonItems && recipe.key.namespace == "minecraft") {
            inventory.result = null
            return
        }

        // Prevent crafting of unresearched items
        val pylonItemResult = PylonItem.fromStack(recipe.result)
        val anyViewerDoesNotHaveResearch = pylonItemResult != null && e.viewers.none {
            it is Player && it.canCraft(pylonItemResult, true)
        }
        if (anyViewerDoesNotHaveResearch) {
            inventory.result = null
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onCook(e: BlockCookEvent) {
        e.recipe ?: return
        if (e.source.isPylonAndIsNot<VanillaCookingItem>() && e.recipe?.key?.namespace == "minecraft") {
            e.isCancelled = true
        }
    }
    // TODO: prevent furnace from using fuel on invalid recipes

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onSmith(e: PrepareSmithingEvent) {
        val inv = e.inventory
        val recipe = inv.recipe
        if (recipe !is Keyed) return

        // Prevent crafting of unresearched items
        val pylonItemResult = PylonItem.fromStack(recipe.result)
        val anyViewerDoesNotHaveResearch = pylonItemResult != null && e.viewers.none {
            it is Player && it.canCraft(pylonItemResult, true)
        }
        if (anyViewerDoesNotHaveResearch) {
            inv.result = null
        }

        if (recipe.key.namespace == "minecraft"
            && (inv.inputMineral.isPylonAndIsNot<VanillaSmithingMineral>()
                    || inv.inputTemplate.isPylonAndIsNot<VanillaSmithingTemplate>()
                    || inv.inputEquipment.isPylonAndIsNot<VanillaSmithingMaterial>())
        ) {
            e.result = null
        }
    }
}