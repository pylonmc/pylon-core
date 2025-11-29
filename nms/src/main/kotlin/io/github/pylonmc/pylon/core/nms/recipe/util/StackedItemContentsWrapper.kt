package io.github.pylonmc.pylon.core.nms.recipe.util

import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.nms.recipe.util.StackedItemContentsWrapper.rawGetter
import io.papermc.paper.inventory.recipe.ItemOrExact
import net.minecraft.world.entity.player.StackedContents
import net.minecraft.world.entity.player.StackedItemContents
import net.minecraft.world.item.ItemStack
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import kotlin.math.min

object StackedItemContentsWrapper {
    var initialized = false
    lateinit var rawGetter: MethodHandle

    fun initialize() {
        if (initialized) return
        val lookup = MethodHandles.privateLookupIn(StackedItemContents::class.java, MethodHandles.lookup())
        rawGetter = lookup.findGetter(StackedItemContents::class.java, "raw", StackedContents::class.java)

        initialized = true
    }
}

fun StackedItemContents.getRaw(): StackedContents<ItemOrExact> = rawGetter.invokeExact(this) as StackedContents<ItemOrExact>

fun StackedItemContents.accountStackPylon(stack: ItemStack, maxStackSize: Int = stack.maxStackSize) {
    if (stack.isEmpty) return

    val min = min(maxStackSize, stack.count)

    // Determine if this is a Pylon item
    if (PylonItem.isPylonItem(stack.bukkitStack)) {
        val r = ItemOrExact.Exact(stack.copy())
        this.getRaw().account(r, min)
        return
    }

    this.accountStack(stack, maxStackSize)
}