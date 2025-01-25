package io.github.pylonmc.pylon.core.item

import io.github.pylonmc.pylon.core.registry.PylonRegistries
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack

class PylonItem(private val template: PylonItemStack) : Keyed {


    val stack
        get() = template.clone()

    constructor(id: NamespacedKey, stack: ItemStack) : this(PylonItemStack(id, stack))

    fun register() {
        PylonRegistries.ITEMS.register(this)
    }

    override fun getKey(): NamespacedKey
        = template.id

    override fun equals(other: Any?): Boolean
        = template.id == other

    override fun hashCode(): Int
        = template.id.hashCode()


}