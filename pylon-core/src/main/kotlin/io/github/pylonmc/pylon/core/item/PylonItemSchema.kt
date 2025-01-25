package io.github.pylonmc.pylon.core.item

import io.github.pylonmc.pylon.core.persistence.PylonDataReader
import io.github.pylonmc.pylon.core.registry.PylonRegistries
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles

open class PylonItemSchema(
    private val template: PylonItem,
) : Keyed {
    val id = template.id

    val stack
        get() = template.clone()
    internal val pylonItemClass = template.javaClass

    internal val loadConstructor: MethodHandle = try {
        MethodHandles.lookup().unreflectConstructor(pylonItemClass.getConstructor(ItemStack::class.java))
    } catch (_: NoSuchMethodException) {
        throw NoSuchMethodException("Item '$key' is missing a load constructor")
    }

    fun register() = apply {
        PylonRegistries.ITEMS.register(this)
    }

    override fun getKey(): NamespacedKey
        = template.id

    override fun equals(other: Any?): Boolean
        = template.id == (other as PylonItemSchema).id

    override fun hashCode(): Int
        = template.id.hashCode()
}