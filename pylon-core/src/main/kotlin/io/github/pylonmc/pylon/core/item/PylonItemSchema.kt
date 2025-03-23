package io.github.pylonmc.pylon.core.item

import io.github.pylonmc.pylon.core.item.PylonItem.Companion.idKey
import io.github.pylonmc.pylon.core.persistence.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.registry.RegistryHandler
import io.github.pylonmc.pylon.core.util.findConstructorMatching
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import java.lang.invoke.MethodHandle

abstract class PylonItemSchema(
    private val key: NamespacedKey,
    @JvmSynthetic internal val itemClass: Class<out PylonItem<PylonItemSchema>>,
) : Keyed, RegistryHandler {

    protected abstract fun getTemplate(): ItemStack

    val itemStack: ItemStack
        get() {
            val addon = PylonRegistry.ADDONS.find { addon -> addon.key.namespace == key.namespace }
            if (addon == null) {
                throw IllegalStateException("Item was registered without an addon")
            }
            return ItemStackBuilder(getTemplate())
                .editMeta { meta -> meta.persistentDataContainer.set(idKey, PylonSerializers.NAMESPACED_KEY, key) }
                .lore(LoreBuilder().addon(addon).build())
                .build()
        }

    @JvmSynthetic
    internal val loadConstructor: MethodHandle = itemClass.findConstructorMatching(
        javaClass,
        ItemStack::class.java
    )
        ?: throw NoSuchMethodException("Item '$key' ($itemClass) is missing a load constructor (PylonItemSchema, ItemStack)")

    fun register() = apply {
        PylonRegistry.ITEMS.register(this)
    }

    override fun getKey(): NamespacedKey = key

    override fun equals(other: Any?): Boolean = key == (other as? PylonItemSchema)?.key

    override fun hashCode(): Int = key.hashCode()
}