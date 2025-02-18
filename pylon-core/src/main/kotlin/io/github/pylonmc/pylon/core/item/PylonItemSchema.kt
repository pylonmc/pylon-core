package io.github.pylonmc.pylon.core.item

import io.github.pylonmc.pylon.core.item.PylonItem.Companion.idKey
import io.github.pylonmc.pylon.core.persistence.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.findConstructorMatching
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import java.lang.invoke.MethodHandle

open class PylonItemSchema(
    private val id: NamespacedKey,
    internal val itemClass: Class<out PylonItem<PylonItemSchema>>,
    private val template: ItemStack,
) : Keyed {
    init {
        template.editMeta { meta ->
            meta.persistentDataContainer.set(idKey, PylonSerializers.NAMESPACED_KEY, id)
        }
    }

    val itemStack: ItemStack
        get() = template.clone()

    internal val loadConstructor: MethodHandle = itemClass.findConstructorMatching(
        PylonItemSchema::class.java,
        ItemStack::class.java
    )
        ?: throw NoSuchMethodException("Item '$key' ($itemClass) is missing a load constructor (PylonItemSchema, ItemStack)")

    fun register() = apply {
        PylonRegistry.ITEMS.register(this)
    }

    override fun getKey(): NamespacedKey = id

    override fun equals(other: Any?): Boolean = id == (other as? PylonItemSchema)?.id

    override fun hashCode(): Int = id.hashCode()
}