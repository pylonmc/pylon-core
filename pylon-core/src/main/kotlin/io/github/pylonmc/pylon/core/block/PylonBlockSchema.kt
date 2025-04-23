package io.github.pylonmc.pylon.core.block

import io.github.pylonmc.pylon.core.block.context.BlockCreateContext
import io.github.pylonmc.pylon.core.item.PylonItemSchema
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.findConstructorMatching
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.persistence.PersistentDataContainer
import java.lang.invoke.MethodHandle

/**
 * The core idea of Pylon with respect to other similar plugins is the distinction between
 * configuration and implementation. [PylonBlockSchema] specifies the global properties of
 * a Pylon block, such as material, name, key, etc. It does *not* specify what the block
 * does when you right-click it, or break it, or look at it. That is the job of [PylonBlock].
 * Thus, there is only ever one instance of a `PylonBlockSchema` per block type, while
 * there are as many instances of `PylonBlock` as there are loaded blocks of that type.
 * The schema also has another very important function: it is the thing used to create
 * the block.
 *
 * @param key The key of the block schema
 * @property material The material of the block, which may be overridden by [getPlaceMaterial]
 * @param blockClass The class of the block which this schema will be used to create
 *
 * @see PylonBlock
 * @see PylonItemSchema
 */
open class PylonBlockSchema(
    private val key: NamespacedKey,
    val material: Material,
    blockClass: Class<out PylonBlock<*>>,
) : Keyed {

    init {
        check(material.isBlock) { "Material $material is not a block" }
    }

    @JvmSynthetic
    internal val createConstructor: MethodHandle = blockClass.findConstructorMatching(
        javaClass,
        Block::class.java,
        BlockCreateContext::class.java
    )
        ?: throw NoSuchMethodException("Block '$key' ($blockClass) is missing a create constructor (PylonBlockSchema, Block, BlockCreateContext)")

    @JvmSynthetic
    internal val loadConstructor: MethodHandle = blockClass.findConstructorMatching(
        javaClass,
        Block::class.java,
        PersistentDataContainer::class.java
    )
        ?: throw NoSuchMethodException("Block '$key' ($blockClass) is missing a load constructor (PylonBlockSchema, Block, PersistentDataContainer)")

    open fun getPlaceMaterial(block: Block, context: BlockCreateContext): Material {
        return material
    }

    fun register() {
        PylonRegistry.BLOCKS.register(this)
    }

    override fun getKey(): NamespacedKey = key
}