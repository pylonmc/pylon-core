package io.github.pylonmc.pylon.core.item

import io.github.pylonmc.pylon.core.block.BlockStorage
import io.github.pylonmc.pylon.core.block.PylonBlock
import io.github.pylonmc.pylon.core.block.context.BlockCreateContext
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.item.research.Research
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.registry.RegistryHandler
import io.github.pylonmc.pylon.core.util.findConstructorMatching
import io.github.pylonmc.pylon.core.util.getAddon
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import java.lang.invoke.MethodHandle

/**
 * Stores information about a Pylon item type, including its key, default [ItemStack], class, and
 * any associated blocks.
 *
 * You should not need to use this if you are not working on Pylon Core.
 */
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

    val research: Research?
        get() = PylonRegistry.RESEARCHES.find { key in it.unlocks }

    val researchBypassPermission = "pylon.item.${key.namespace}.${key.key}"

    @JvmSynthetic
    internal val loadConstructor: MethodHandle = itemClass.findConstructorMatching(ItemStack::class.java)
        ?: throw NoSuchMethodException("Item '$key' (${itemClass.simpleName}) is missing a load constructor (ItemStack)")

    /**
     * Attempts to place the block associated with this item.
     *
     * Does nothing if no block is associated with this item.
     */
    fun place(context: BlockCreateContext): PylonBlock? {
        if (pylonBlockKey == null) {
            return null
        }
        val blockSchema = PylonRegistry.BLOCKS[pylonBlockKey]
        check(blockSchema != null) { "Block $pylonBlockKey not found" }
        check(template.type == blockSchema.material) {
            "Item $key places block $pylonBlockKey and so must have the same type - but the item is of type + ${template.type} while the block is of type ${blockSchema.material}"
        }
        if (BlockStorage.isPylonBlock(context.block)) { // special case: you can place on top of structure void blocks
            return null
        }
        return BlockStorage.placeBlock(context.block, pylonBlockKey, context)
    }

    override fun getKey(): NamespacedKey = key

    override fun equals(other: Any?): Boolean = key == (other as? PylonItemSchema)?.key

    override fun hashCode(): Int = key.hashCode()

    companion object {
        val pylonItemKeyKey = pylonKey("pylon_item_key")
    }
}