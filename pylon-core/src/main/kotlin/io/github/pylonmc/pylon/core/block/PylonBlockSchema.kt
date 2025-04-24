package io.github.pylonmc.pylon.core.block

import io.github.pylonmc.pylon.core.block.context.BlockCreateContext
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.persistence.PersistentDataContainer
import org.jetbrains.annotations.ApiStatus

abstract class PylonBlockSchema(
    private val key: NamespacedKey,
    val material: Material
) : Keyed {

    init {
        check(material.isBlock) { "Material $material is not a block" }
    }

    val addon = PylonRegistry.ADDONS.find { addon -> addon.key.namespace == key.namespace }
        ?: error("Block does not have a corresponding addon, does your plugin call registerWithPylon()?")

    val settings = addon.mergeGlobalConfig("settings/block/${key.namespace}/${key.key}.yml")

    open fun getPlaceMaterial(block: Block, context: BlockCreateContext): Material {
        return material
    }

    @ApiStatus.OverrideOnly
    abstract fun createBlock(block: Block, context: BlockCreateContext): PylonBlock<*>

    @ApiStatus.OverrideOnly
    abstract fun loadBlock(block: Block, pdc: PersistentDataContainer): PylonBlock<*>

    fun register() {
        PylonRegistry.BLOCKS.register(this)
    }

    override fun getKey(): NamespacedKey = key

    private class SimplePylonBlockSchema(
        key: NamespacedKey,
        material: Material,
        private val createFunction: PylonBlockSchema.(Block, BlockCreateContext) -> PylonBlock<*>,
        private val loadFunction: PylonBlockSchema.(Block, PersistentDataContainer) -> PylonBlock<*>
    ) : PylonBlockSchema(key, material) {

        override fun createBlock(block: Block, context: BlockCreateContext): PylonBlock<*> {
            return createFunction(block, context)
        }

        override fun loadBlock(block: Block, pdc: PersistentDataContainer): PylonBlock<*> {
            return loadFunction(block, pdc)
        }
    }

    companion object {

        @JvmStatic
        @JvmOverloads
        fun simpleWithContext(
            key: NamespacedKey,
            material: Material,
            createFunction: PylonBlockSchema.(Block, BlockCreateContext) -> PylonBlock<*>,
            loadFunction: PylonBlockSchema.(Block, PersistentDataContainer) -> PylonBlock<*> = { block, _ ->
                createFunction(block, BlockCreateContext.Default)
            }
        ): PylonBlockSchema {
            return SimplePylonBlockSchema(key, material, createFunction, loadFunction)
        }

        @JvmStatic
        @JvmOverloads
        fun simple(
            key: NamespacedKey,
            material: Material,
            createFunction: PylonBlockSchema.(Block) -> PylonBlock<*>,
            loadFunction: PylonBlockSchema.(Block, PersistentDataContainer) -> PylonBlock<*> = { block, _ ->
                createFunction(block)
            }
        ): PylonBlockSchema {
            return SimplePylonBlockSchema(
                key,
                material,
                { block, _ -> createFunction(this, block) },
                loadFunction
            )
        }
    }
}