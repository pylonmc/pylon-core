package io.github.pylonmc.pylon.core.block

import io.github.pylonmc.pylon.core.block.context.BlockCreateContext
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.findConstructorMatching
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.persistence.PersistentDataContainer
import java.lang.invoke.MethodHandle

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