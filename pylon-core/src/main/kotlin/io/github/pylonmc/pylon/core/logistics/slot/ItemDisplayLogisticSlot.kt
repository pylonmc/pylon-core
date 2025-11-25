package io.github.pylonmc.pylon.core.logistics.slot

import io.github.pylonmc.pylon.core.logistics.LogisticSlot
import org.bukkit.entity.ItemDisplay
import org.bukkit.inventory.ItemStack
import java.lang.ref.WeakReference

open class ItemDisplayLogisticSlot(
    displayIn: ItemDisplay,
    val max: Long? = null
) : LogisticSlot {

    // Held as weakref to prevent display from being persisted in memory after despawning
    private val display = WeakReference(displayIn)

    override fun getItemStack() = display.get()?.itemStack

    override fun getMaxAmount(stack: ItemStack) = max ?: super.getMaxAmount(stack)

    override fun set(stack: ItemStack?, amount: Long) {
        display.get()?.setItemStack(stack?.asQuantity(amount.toInt()))
    }
}