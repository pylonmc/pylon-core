package io.github.pylonmc.pylon.core.item

import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.item.PylonItem.Companion.idKey
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.registry.RegistryHandler
import io.github.pylonmc.pylon.core.util.findConstructorMatching
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import java.lang.invoke.MethodHandle
import java.util.function.Function

open class PylonItemSchema(
    private val key: NamespacedKey,
    @JvmSynthetic internal val itemClass: Class<out PylonItem<PylonItemSchema>>,
    @JvmField protected val template: ItemStack
) : Keyed, RegistryHandler {

    constructor(
        key: NamespacedKey,
        itemClass: Class<out PylonItem<PylonItemSchema>>,
        templateSupplier: Function<NamespacedKey, ItemStack>
    ) : this(key, itemClass, templateSupplier.apply(key))

    val addon = PylonRegistry.ADDONS.find { addon -> addon.key.namespace == key.namespace }
        ?: error("Item does not have a corresponding addon; does your plugin call registerWithPylon()?")

    val itemStack: ItemStack
        get() = template.clone()

    val researchBypassPermission = "pylon.item.${key.namespace}.${key.key}"

    @JvmSynthetic
    internal val loadConstructor: MethodHandle = itemClass.findConstructorMatching(
        javaClass,
        ItemStack::class.java
    ) ?: throw NoSuchMethodException(
        "Item '$key' ($itemClass) is missing a load constructor (${javaClass.simpleName}, ItemStack)"
    )

    val settings = addon.mergeGlobalConfig("settings/item/${key.namespace}/${key.key}.yml")

    fun register() = apply {
        template.editMeta { meta -> meta.persistentDataContainer.set(idKey, PylonSerializers.NAMESPACED_KEY, key) }
        PylonRegistry.ITEMS.register(this)
    }

    override fun getKey(): NamespacedKey = key

    override fun equals(other: Any?): Boolean = key == (other as? PylonItemSchema)?.key

    override fun hashCode(): Int = key.hashCode()
}