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

    val addon = PylonRegistry.ADDONS.find { addon -> addon.key.namespace == key.namespace }
        ?: error("Block does not have a corresponding addon, does your plugin call registerWithPylon()?")

    @JvmSynthetic
    internal val createConstructor: MethodHandle = blockClass.findConstructorMatching(
        javaClass,
        Block::class.java,
        BlockCreateContext::class.java
    ) ?: throw NoSuchMethodException(
        "Block '$key' ($blockClass) is missing a create constructor (${javaClass.simpleName}, Block, BlockCreateContext)"
    )

    @JvmSynthetic
    internal val loadConstructor: MethodHandle = blockClass.findConstructorMatching(
        javaClass,
        Block::class.java,
        PersistentDataContainer::class.java
    ) ?: throw NoSuchMethodException(
        "Block '$key' ($blockClass) is missing a load constructor (${javaClass.simpleName}, Block, PersistentDataContainer)"
    )

    open fun getPlaceMaterial(block: Block, context: BlockCreateContext): Material {
        return material
    }

    val settings = addon.mergeGlobalConfig("settings/block/${key.namespace}/${key.key}.yml")

    fun register() {
        PylonRegistry.BLOCKS.register(this)
    }

    override fun getKey(): NamespacedKey = key
}