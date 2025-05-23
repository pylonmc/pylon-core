package io.github.pylonmc.pylon.core.item

import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.registry.RegistryHandler
import io.github.pylonmc.pylon.core.util.findConstructorMatching
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import java.lang.invoke.MethodHandle

class PylonItemSchema internal constructor(
    @JvmSynthetic internal val itemClass: Class<out PylonItem>,
    private val template: ItemStack
) : Keyed, RegistryHandler {

    private val key = template.persistentDataContainer.get(idKey, PylonSerializers.NAMESPACED_KEY)
        ?: throw IllegalArgumentException("Provided item stack is not a Pylon item; make sure you are using ItemStackBuilder.defaultBuilder to create the item stack")

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

    override fun getKey(): NamespacedKey = key

    override fun equals(other: Any?): Boolean = key == (other as? PylonItemSchema)?.key

    override fun hashCode(): Int = key.hashCode()

    companion object {
        val idKey = pylonKey("pylon_id")
    }
}