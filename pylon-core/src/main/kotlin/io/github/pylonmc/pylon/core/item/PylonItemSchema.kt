package io.github.pylonmc.pylon.core.item

import io.github.pylonmc.pylon.core.item.PylonItem.Companion.idKey
import io.github.pylonmc.pylon.core.persistence.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.registry.PylonRegistryKey
import io.github.pylonmc.pylon.core.registry.RegistryHandler
import io.github.pylonmc.pylon.core.util.findConstructorMatching
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.MustBeInvokedByOverriders
import java.lang.invoke.MethodHandle

open class PylonItemSchema(
    private val key: NamespacedKey,
    internal val itemClass: Class<out PylonItem<PylonItemSchema>>,
    private val template: ItemStack,
) : Keyed, RegistryHandler {

    private var alreadyRegistered = false

    init {
        template.editMeta { meta ->
            meta.persistentDataContainer.set(idKey, PylonSerializers.NAMESPACED_KEY, key)
        }
    }

    val itemStack: ItemStack
        get() = template.clone()

    internal val loadConstructor: MethodHandle = itemClass.findConstructorMatching(
        javaClass,
        ItemStack::class.java
    )
        ?: throw NoSuchMethodException("Item '$key' ($itemClass) is missing a load constructor (PylonItemSchema, ItemStack)")

    fun register() = apply {
        PylonRegistry.ITEMS.register(this)
    }

    @MustBeInvokedByOverriders
    override fun onRegister(registry: PylonRegistry<*>) {
        if (registry.key != PylonRegistryKey.ITEMS || alreadyRegistered) return

        alreadyRegistered = true
        template.editMeta { meta ->
            val plugin = Bukkit.getPluginManager().plugins.first {
                it.name.equals(key.namespace, ignoreCase = true)
            }
            val lore = meta.lore() ?: mutableListOf()
            lore.add(
                Component.text(plugin.name)
                    .color(NamedTextColor.BLUE)
                    .decoration(TextDecoration.ITALIC, true)
            )
            meta.lore(lore)
        }
    }

    override fun getKey(): NamespacedKey = key

    override fun equals(other: Any?): Boolean = key == (other as? PylonItemSchema)?.key

    override fun hashCode(): Int = key.hashCode()
}