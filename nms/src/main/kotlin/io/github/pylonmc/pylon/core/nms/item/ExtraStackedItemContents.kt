package io.github.pylonmc.pylon.core.nms.item

import io.github.pylonmc.pylon.core.item.PylonItem
import io.papermc.paper.inventory.recipe.ItemOrExact
import io.papermc.paper.inventory.recipe.StackedContentsExtrasMap
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.StackedContents
import net.minecraft.world.entity.player.StackedContents.IngredientInfo
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.CraftingInput
import net.minecraft.world.item.crafting.Recipe
import kotlin.math.min


class ExtraStackedItemContents {
    private val raw = StackedContents<ItemOrExact?>()
    private var extrasMap: StackedContentsExtrasMap? = null

    fun accountSimpleStack(stack: ItemStack) {
        if (this.extrasMap != null && this.extrasMap!!.accountStack(
                stack,
                min(64, stack.count)
            )
        ) return  // Paper - Improve exact choice recipe ingredients; max of 64 due to accountStack method below

        if (Inventory.isUsableForCrafting(stack)) {
            this.accountStack(stack)
        }
    }

    fun accountStack(stack: ItemStack) {
        this.accountStack(stack, stack.maxStackSize)
    }

    fun accountStack(stack: ItemStack, maxStackSize: Int) {
        if (stack.isEmpty) return

        val min = min(maxStackSize, stack.count)

        // Determine if this is a Pylon item
        val isPylon = PylonItem.isPylonItem(stack.bukkitStack)

        if (isPylon) {
            val r = ItemOrExact.Exact(stack.copy())
            this.raw.account(r, min)
            return
        } else {
            val r = ItemOrExact.Item(stack.copy())
            this.raw.account(r, min)
        }

        // Extras map is still used for exact matching of Pylon items
        if (this.extrasMap == null) {
            this.extrasMap = StackedContentsExtrasMap(this.raw)
        }
        this.extrasMap!!.accountStack(stack, min)
    }

    fun canCraft(
        recipe: Recipe<*>,
        output: StackedContents.Output<ItemOrExact?>?
    ): Boolean {
        return this.canCraft(recipe, 1, output)
    }

    fun initializeExtras(recipe: Recipe<*>, input: CraftingInput?) {
        if (this.extrasMap == null) {
            this.extrasMap = StackedContentsExtrasMap(this.raw)
        }
        this.extrasMap!!.initialize(recipe)
        if (input != null) this.extrasMap!!.accountInput(input)
    }

    fun resetExtras() {
        if (this.extrasMap != null && !this.raw.amounts.isEmpty()) {
            this.extrasMap!!.resetExtras()
        }
    }

    fun canCraft(
        recipe: Recipe<*>,
        maxCount: Int,
        output: StackedContents.Output<ItemOrExact?>?
    ): Boolean {
        val placementInfo = recipe.placementInfo()
        return !placementInfo.isImpossibleToPlace && this.canCraft(placementInfo.ingredients(), maxCount, output)
    }

    private fun canCraft(
        ingredients: MutableList<out IngredientInfo<ItemOrExact?>?>,
        maxCount: Int,
        output: StackedContents.Output<ItemOrExact?>?
    ): Boolean {
        return this.raw.tryPick(wrapIngredients(ingredients), maxCount, output)
    }

    fun getBiggestCraftableStack(
        recipe: Recipe<*>,
        output: StackedContents.Output<ItemOrExact?>?
    ): Int { // Paper - Improve exact choice recipe ingredients
        return this.getBiggestCraftableStack(recipe, Int.MAX_VALUE, output)
    }

    fun getBiggestCraftableStack(
        recipe: Recipe<*>,
        maxCount: Int,
        output: StackedContents.Output<ItemOrExact?>?
    ): Int {
        return this.raw.tryPickAll(wrapIngredients(recipe.placementInfo().ingredients()), maxCount, output)
    }

    fun clear() {
        this.raw.clear()
    }
}
