package io.github.pylonmc.pylon.core.nms.item

import io.github.pylonmc.pylon.core.item.PylonItem
import io.papermc.paper.inventory.recipe.ItemOrExact
import net.minecraft.world.entity.player.StackedContents
import net.minecraft.world.entity.player.StackedItemContents
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.CraftingInput
import net.minecraft.world.item.crafting.Recipe
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import kotlin.math.min


class ExtraStackedItemContents {
    private val delegate = StackedItemContents()

    init {
        initialize()
    }

    fun accountStack(stack: ItemStack) {
        this.accountStack(stack, stack.maxStackSize)
    }

    fun accountStack(stack: ItemStack, maxStackSize: Int) {
        if (stack.isEmpty) return

        val min = min(maxStackSize, stack.count)

        // Determine if this is a Pylon item
        if (PylonItem.isPylonItem(stack.bukkitStack)) {
            val r = ItemOrExact.Exact(stack.copy())
            delegate.getRaw().account(r, min)
            return
        }

        delegate.accountStack(stack, maxStackSize)
    }

    fun canCraft(
        recipe: Recipe<*>,
        output: StackedContents.Output<ItemOrExact?>?
    ): Boolean {
        return this.canCraft(recipe, 1, output)
    }

    fun initializeExtras(recipe: Recipe<*>, input: CraftingInput?) {
        delegate.initializeExtras(recipe, input)
    }

    fun canCraft(
        recipe: Recipe<*>,
        maxCount: Int,
        output: StackedContents.Output<ItemOrExact?>?
    ): Boolean {
        return delegate.canCraft(recipe, maxCount, output)
    }

    fun getBiggestCraftableStack(
        recipe: Recipe<*>,
        output: StackedContents.Output<ItemOrExact?>?
    ): Int {
        return this.getBiggestCraftableStack(recipe, Int.MAX_VALUE, output)
    }

    fun getBiggestCraftableStack(
        recipe: Recipe<*>,
        maxCount: Int,
        output: StackedContents.Output<ItemOrExact?>?
    ): Int {
        return delegate.getBiggestCraftableStack(recipe, maxCount, output)
    }

    companion object {
        var initialized = false
        lateinit var rawGetter: MethodHandle

        fun initialize() {
            if (initialized) return
            val lookup = MethodHandles.privateLookupIn(StackedItemContents::class.java, MethodHandles.lookup())
            rawGetter = lookup.findGetter(StackedItemContents::class.java, "raw", StackedContents::class.java)

            initialized = true
        }

        fun StackedItemContents.getRaw(): StackedContents<ItemOrExact> {
            return rawGetter.invokeExact(this) as StackedContents<ItemOrExact>
        }
    }
}
