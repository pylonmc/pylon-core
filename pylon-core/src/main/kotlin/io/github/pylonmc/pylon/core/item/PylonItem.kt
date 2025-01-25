package io.github.pylonmc.pylon.core.item

import io.github.pylonmc.pylon.core.persistence.PylonSerializers
import io.github.pylonmc.pylon.core.pluginInstance
import io.github.pylonmc.pylon.core.registry.PylonRegistries
import org.bukkit.NamespacedKey
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.FurnaceBurnEvent
import org.bukkit.event.inventory.FurnaceExtractEvent
import org.bukkit.event.inventory.FurnaceSmeltEvent
import org.bukkit.event.inventory.FurnaceStartSmeltEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

open class PylonItem protected constructor(stack: ItemStack) : ItemStack(stack) {
    val id = persistentDataContainer.get(idKey, PylonSerializers.NAMESPACED_KEY)!!

    constructor(id: NamespacedKey, stack: ItemStack) : this(setId(stack, id))

    fun register() {
        PylonItemSchema(this).register()
    }

    override fun equals(other: Any?): Boolean
            = id == (other as PylonItem).id

    override fun hashCode(): Int
            = id.hashCode()

    /**
     * Called when the item is crafted in a crafting matrix
     */
    fun onCrafted(event: CraftItemEvent) {}

    /**
     * Called when the item is burnt as fuel in a furnace, smoker, etc
     */
    fun onBurntAsFuel(event: FurnaceBurnEvent) {}

    /**
     * Called when the item is extracted from a furnace, smoker, etc
     */
    fun onExtractedFromFurnace(event: FurnaceExtractEvent) {}

    /**
     * Called when the item is produced as the result of smelting in a furnace, smoker, etc
     */
    fun onSmelted(event: FurnaceSmeltEvent) {}

    /**
     * Called when the item starts being smelted in a furnace, smoker, etc
     */
    fun onStartsBeingSmelted(event: FurnaceStartSmeltEvent) {}

    /**
     * Called when the item is clicked in any inventory
     */
    fun onClickedInInventory(event: InventoryClickEvent) {}

    companion object {
        val idKey = NamespacedKey(pluginInstance, "pylon_id")

        /**
         * Converts a regular ItemStack to a PylonItemStack
         * Returns null if the ItemStack is not a Pylon item
         */
        fun fromStack(stack: ItemStack): PylonItem? {
            val id = stack.persistentDataContainer.get(idKey, PylonSerializers.NAMESPACED_KEY)
                ?: return null
            val item = PylonRegistries.ITEMS[id]
                ?: return null
            return item.pylonItemClass.cast(item.loadConstructor.invoke(stack))
        }

        /**
         * Convenience function for use in constructor
         */
        private fun setId(stack: ItemStack, id: NamespacedKey): ItemStack {
            stack.editMeta { meta ->
                meta.persistentDataContainer.set(idKey, PylonSerializers.NAMESPACED_KEY, id)
            }
            return stack
        }
    }
}