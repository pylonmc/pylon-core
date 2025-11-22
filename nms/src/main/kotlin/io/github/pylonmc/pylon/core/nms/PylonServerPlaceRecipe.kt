package io.github.pylonmc.pylon.core.nms

import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.nms.item.ExtraStackedItemContents
import io.papermc.paper.inventory.recipe.ItemOrExact
import net.minecraft.recipebook.PlaceRecipeHelper
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.AbstractCraftingMenu
import net.minecraft.world.inventory.RecipeBookMenu.PostPlaceAction
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.CraftingRecipe
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeHolder
import net.minecraft.world.item.crafting.RecipeInput
import java.util.*
import kotlin.math.min

/**
 * This is a slightly changed copy of net.minecraft.recipebook.ServerPlaceRecipe
 */
class PylonServerPlaceRecipe(
    private val menu: AbstractCraftingMenu,
    private val player: ServerPlayer,
    private val useMaxItems: Boolean,
    private val inputGridSlots: MutableList<Slot>,
    private val slotsToClear: MutableList<Slot>
) {

    fun clearCraftingContent() {
        menu.resultSlots.clearContent()
        menu.craftSlots.clearContent()
    }

    fun recipeMatches(recipe1: RecipeHolder<CraftingRecipe>): Boolean {
        return recipe1.value()!!.matches(
            menu.craftSlots.asCraftInput(),
            player.level()
        )
    }

    fun fillCraftSlotsStackedContents(stackedItemContents: ExtraStackedItemContents) {
        for (stack in menu.craftSlots.contents) {
            stackedItemContents.accountStack(stack)
        }
    }

    private fun tryPlaceRecipe(recipe: RecipeHolder<CraftingRecipe>, stackedItemContents: ExtraStackedItemContents): PostPlaceAction {
        if (stackedItemContents.canCraft(recipe.value()!!, null)) {
            this.placeRecipe(recipe, stackedItemContents)
            this.player.inventory.setChanged()
            return PostPlaceAction.NOTHING
        } else {
            this.clearGrid()
            this.player.inventory.setChanged()
            return PostPlaceAction.PLACE_GHOST_RECIPE
        }
    }

    private fun clearGrid() {
        for (slot in this.slotsToClear) {
            val itemStack = slot.item.copy()
            this.player.inventory.placeItemBackInInventory(itemStack, false)
            slot.set(itemStack)
        }

        this.clearCraftingContent()
    }

    private fun placeRecipe(recipe: RecipeHolder<CraftingRecipe>, stackedItemContents: ExtraStackedItemContents) {
        val flag = this.recipeMatches(recipe)
        val rcp = recipe.value()!!
        val biggestCraftableStack = stackedItemContents.getBiggestCraftableStack(rcp, null)
        if (flag) {
            for (slot in this.inputGridSlots) {
                val item = slot.item
                if (!item.isEmpty && min(biggestCraftableStack, item.maxStackSize) < item.count + 1) {
                    return
                }
            }
        }

        val i = this.calculateAmountToCraft(biggestCraftableStack, flag)
        val list: MutableList<ItemOrExact> = ArrayList()
        var selectedRecipe: Recipe<*> = recipe.value()!!
        Objects.requireNonNull(list)
        if (stackedItemContents.canCraft(selectedRecipe, i) { e: ItemOrExact? -> list.add(e!!) }) {
            val i1: Int = clampToMaxStackSize(i, list)
            if (i1 != i) {
                list.clear()
                selectedRecipe = recipe.value()!!
                Objects.requireNonNull(list)
                if (!stackedItemContents.canCraft(
                        selectedRecipe,
                        i1
                    ) { e: ItemOrExact? -> list.add(e!!) }
                ) {
                    return
                }
            }

            this.clearGrid()
            PlaceRecipeHelper.placeRecipe(
                this.menu.gridWidth,
                this.menu.gridHeight,
                recipe.value(),
                recipe.value()!!.placementInfo().slotsToIngredientIndex()
            ) { item1: Int?, slot1: Int, _: Int, _: Int ->
                if (item1 != -1) {
                    val slot2 = this.inputGridSlots[slot1]
                    val holder = list[item1!!]
                    var i2 = i1

                    while (i2 > 0) {
                        i2 = this.moveItemToGrid(slot2, holder, i2)
                        if (i2 == -1) {
                            return@placeRecipe
                        }
                    }
                }
            }
        }
    }

    private fun calculateAmountToCraft(max: Int, recipeMatches: Boolean): Int {
        if (this.useMaxItems) {
            return max
        }

        if (!recipeMatches) {
            return 1
        }

        var i = Int.MAX_VALUE

        for (slot in this.inputGridSlots) {
            val item = slot.item
            if (!item.isEmpty && i > item.count) {
                i = item.count
            }
        }

        if (i != Int.MAX_VALUE) {
            ++i
        }

        return i
    }

    private fun moveItemToGrid(slot: Slot, item: ItemOrExact, count: Int): Int {
        val item1 = slot.item
        val i = findSlotMatchingCraftingIngredient(this.player.inventory.contents, item, item1)
        if (i == -1) {
            return -1
        }

        val item2 = this.player.inventory.getItem(i)
        val itemStack = if (count < item2.count) {
            this.player.inventory.removeItem(i, count)
        } else {
            this.player.inventory.removeItemNoUpdate(i)
        }

        val count1 = itemStack.count
        if (item1.isEmpty) {
            slot.set(itemStack)
        } else {
            item1.grow(count1)
        }

        return count - count1
    }

    private fun testClearGrid(): Boolean {
        val list: MutableList<ItemStack> = ArrayList()
        val amountOfFreeSlotsInInventory = this.amountOfFreeSlotsInInventory

        for (slot in this.inputGridSlots) {
            val itemStack = slot.item.copy()
            if (itemStack.isEmpty) continue

            val slotWithRemainingSpace = this.player.inventory.getSlotWithRemainingSpace(itemStack)
            if (slotWithRemainingSpace == -1 && list.size <= amountOfFreeSlotsInInventory) {
                for (itemStack1 in list) {
                    if (ItemStack.isSameItem(
                            itemStack1,
                            itemStack
                        ) && itemStack1.count != itemStack1.maxStackSize && itemStack1.count + itemStack.count <= itemStack1.maxStackSize
                    ) {
                        itemStack1.grow(itemStack.count)
                        itemStack.count = 0
                        break
                    }
                }

                if (!itemStack.isEmpty) {
                    if (list.size >= amountOfFreeSlotsInInventory) {
                        return false
                    }

                    list.add(itemStack)
                }
            } else if (slotWithRemainingSpace == -1) {
                return false
            }
        }

        return true
    }

    private val amountOfFreeSlotsInInventory = this.player.inventory.nonEquipmentItems.count { it.isEmpty }

    fun findSlotMatchingCraftingIngredient(items: List<ItemStack>, item: ItemOrExact, stack: ItemStack): Int {
        for (i in items.indices) {
            val itemStack = items[i]
            if (itemStack.isEmpty) continue

            if (!item.matches(itemStack)) continue

            if (item is ItemOrExact.Item) {
                if (!Inventory.isUsableForCrafting(itemStack)) continue
                if (PylonItem.isPylonItem(itemStack.bukkitStack)) continue // skip our pylon items
            }

            if (stack.isEmpty || ItemStack.isSameItemSameComponents(stack, itemStack)) {
                return i
            }
        }

        return -1
    }

    companion object {
        fun placeRecipe(
            menu: AbstractCraftingMenu,
            player: ServerPlayer,
            inputGridSlots: MutableList<Slot>,
            slotsToClear: MutableList<Slot>,
            recipe: RecipeHolder<CraftingRecipe>,
            useMaxItems: Boolean
        ): PostPlaceAction {
            val serverPlaceRecipe = PylonServerPlaceRecipe(
                menu,
                player,
                useMaxItems,
                inputGridSlots,
                slotsToClear
            )
            if (!player.isCreative && !serverPlaceRecipe.testClearGrid()) {
                return PostPlaceAction.NOTHING
            }

            val stackedItemContents = ExtraStackedItemContents()
            stackedItemContents.initializeExtras(recipe.value()!!, null)

            for (itemStack in player.inventory) {
                stackedItemContents.accountStack(itemStack)
            }

            serverPlaceRecipe.fillCraftSlotsStackedContents(stackedItemContents)
            return serverPlaceRecipe.tryPlaceRecipe(recipe, stackedItemContents)
        }

        private fun clampToMaxStackSize(amount: Int, items: MutableList<ItemOrExact>): Int {
            var amount = amount
            for (holder in items) {
                amount = min(amount, holder.maxStackSize)
            }

            return amount
        }
    }
}
