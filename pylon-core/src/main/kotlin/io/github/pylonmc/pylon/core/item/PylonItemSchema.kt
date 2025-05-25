package io.github.pylonmc.pylon.core.item

import io.github.pylonmc.pylon.core.block.BlockStorage
import io.github.pylonmc.pylon.core.block.PylonBlock
import io.github.pylonmc.pylon.core.block.context.BlockCreateContext
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.registry.RegistryHandler
import io.github.pylonmc.pylon.core.util.findConstructorMatching
import io.github.pylonmc.pylon.core.util.key.getAddon
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack
import java.lang.invoke.MethodHandle

class PylonItemSchema @JvmOverloads internal constructor(
    @JvmSynthetic internal val itemClass: Class<out PylonItem>,
    private val template: ItemStack,
    val pylonBlockKey: NamespacedKey? = null
) : Keyed, RegistryHandler {

    private val key = template.persistentDataContainer.get(pylonItemKeyKey, PylonSerializers.NAMESPACED_KEY)
        ?: throw IllegalArgumentException("Provided item stack is not a Pylon item; make sure you are using ItemStackBuilder.defaultBuilder to create the item stack")

    val addon = getAddon(key)

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

    fun place(context: BlockCreateContext, block: Block): PylonBlock? {
        if (pylonBlockKey == null) {
            return null
        }
        check(template.type.isBlock) { "Material ${template.type} is not a block" }
        if (BlockStorage.isPylonBlock(block)) { // special case: you can place on top of structure void blocks
            return null
        }
        return BlockStorage.placeBlock(block, pylonBlockKey, context)
    }

    override fun getKey(): NamespacedKey = key

    override fun equals(other: Any?): Boolean = key == (other as? PylonItemSchema)?.key

    override fun hashCode(): Int = key.hashCode()

    companion object {
        val pylonItemKeyKey = pylonKey("pylon_item_key")
    }
}