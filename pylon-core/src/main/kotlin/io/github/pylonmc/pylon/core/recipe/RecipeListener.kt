package io.github.pylonmc.pylon.core.recipe

import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.item.base.*
import io.github.pylonmc.pylon.core.item.research.Research.Companion.canCraft
import io.github.pylonmc.pylon.core.recipe.vanilla.CookingRecipeWrapper
import io.github.pylonmc.pylon.core.recipe.vanilla.VanillaRecipeType
import io.github.pylonmc.pylon.core.util.isPylonAndIsNot
import io.github.pylonmc.pylon.core.util.isPylonSimilar
import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.Keyed
import org.bukkit.block.Crafter
import org.bukkit.block.Furnace
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockCookEvent
import org.bukkit.event.block.CrafterCraftEvent
import org.bukkit.event.inventory.FurnaceBurnEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.event.inventory.PrepareSmithingEvent
import org.bukkit.inventory.ItemStack

internal object PylonRecipeListener : Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onPreCraft(e: PrepareItemCraftEvent) {
        val recipe = e.recipe
        // All recipe types but MerchantRecipe implement Keyed
        if (recipe !is Keyed) return
        val inventory = e.inventory

        val hasPylonItems = inventory.any { it.isPylonAndIsNot<VanillaCraftingItem>() }
        val isNotPylonCraftingRecipe = recipe.key in VanillaRecipeType.nonPylonRecipes

        // Prevent the erroneous crafting of vanilla items with Pylon ingredients
        if (hasPylonItems && isNotPylonCraftingRecipe) {
            inventory.result = null
        }

        // Allow merging Pylon tools/weapons/armour in crafting grid unless marked with PylonUnmergeable
        if (hasPylonItems && e.isRepair) {
            var firstItem: ItemStack? = null
            var secondItem: ItemStack? = null
            for (item in e.inventory.matrix) {
                if (item != null && !item.isEmpty)  {
                    if (firstItem == null) {
                        firstItem = item
                    } else if (secondItem == null) {
                        secondItem = item
                    } else {
                        error("How the hell is it possible that there are more than two items in an item repair recipe")
                    }
                }
            }
            check(firstItem != null)
            check(secondItem != null)
            if (firstItem.isPylonSimilar(secondItem)) {
                val pylonItem = PylonItem.fromStack(firstItem)!!
                if (pylonItem !is PylonUnmergeable) {
                    val result = pylonItem.schema.getItemStack()
                    val resultDamage = inventory.result!!.getData(DataComponentTypes.DAMAGE)!!
                    result.setData(DataComponentTypes.DAMAGE, resultDamage)
                    inventory.result = result
                }
            } else {
                inventory.result = null
            }
        } else {
            inventory.result = null
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
    private fun onCrafterCraft(e: CrafterCraftEvent) {
        val crafterState = e.block.state as? Crafter ?: return
        val inventory = crafterState.inventory

        val hasPylonItems = inventory.any { it.isPylonAndIsNot<VanillaCraftingItem>() }

        if (hasPylonItems) {
            e.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onCook(e: BlockCookEvent) {
        if (PylonItem.fromStack(e.source) == null) return

        var pylonRecipe: CookingRecipeWrapper? = null
        for (recipe in RecipeType.vanillaCookingRecipes()) {
            if (recipe.key !in VanillaRecipeType.nonPylonRecipes && recipe.recipe.inputChoice.test(e.source)) {
                e.result = recipe.recipe.result.clone()
                pylonRecipe = recipe
                break
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onFuelBurn(e: FurnaceBurnEvent) {
        if (e.fuel.isPylonAndIsNot<VanillaCookingFuel>()) {
            e.isCancelled = true
            return
        }

        val furnace = (e.block.state as Furnace)
        val input = furnace.inventory.smelting
        if (input != null && input.isPylonAndIsNot<VanillaCookingItem>()) {
            var pylonRecipe: CookingRecipeWrapper? = null
            for (recipe in RecipeType.vanillaCookingRecipes()) {
                if (recipe.key !in VanillaRecipeType.nonPylonRecipes && recipe.recipe.inputChoice.test(input)) {
                    pylonRecipe = recipe
                    break
                }
            }
            val isFurnaceOutputValidToPutRecipeResultIn = pylonRecipe != null
                    && (furnace.inventory.result == null || pylonRecipe.isOutput(furnace.inventory.result!!))
            if (pylonRecipe == null || !isFurnaceOutputValidToPutRecipeResultIn) {
                e.isCancelled = true
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onSmith(e: PrepareSmithingEvent) {
        val inv = e.inventory
        val recipe = inv.recipe
        if (recipe !is Keyed) return

        // Prevent the erroneous smithing of vanilla items with Pylon ingredients
        val hasPylonItem = inv.inputMineral.isPylonAndIsNot<VanillaSmithingMineral>()
                || inv.inputTemplate.isPylonAndIsNot<VanillaSmithingTemplate>()
        if (hasPylonItem && recipe.key in VanillaRecipeType.nonPylonRecipes) {
            e.result = null
            return
        }

        // Prevent crafting of unresearched items
        val pylonItemResult = PylonItem.fromStack(recipe.result)
        val anyViewerDoesNotHaveResearch = pylonItemResult != null && e.viewers.none {
            it is Player && it.canCraft(pylonItemResult, true)
        }
        if (anyViewerDoesNotHaveResearch) {
            inv.result = null
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onAnvilSlotChanged(e: PrepareAnvilEvent) {
        val inventory = e.inventory
        val firstItem = inventory.firstItem
        val secondItem = inventory.secondItem
        val firstPylonItem = PylonItem.fromStack(firstItem)
        val secondPylonItem = PylonItem.fromStack(secondItem)

        // Disallow mixing Pylon and non-Pylon items
        if (firstPylonItem != null || secondPylonItem != null) {
            e.result = null
            return
        }
    }
}