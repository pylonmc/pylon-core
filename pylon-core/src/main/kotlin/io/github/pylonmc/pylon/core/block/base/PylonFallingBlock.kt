package io.github.pylonmc.pylon.core.block.base

import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.block.BlockStorage
import io.github.pylonmc.pylon.core.block.PylonBlock
import io.github.pylonmc.pylon.core.block.PylonBlock.Companion.pylonBlockPositionKey
import io.github.pylonmc.pylon.core.block.PylonBlockSchema
import io.github.pylonmc.pylon.core.datatypes.NamespacedKeyPersistentDataType
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.entity.PylonEntity
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.position.BlockPosition
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.entity.FallingBlock
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

interface PylonFallingBlock {
    fun onFallStart(event: EntityChangeBlockEvent) {

    }

    fun onFallStop(event: EntityChangeBlockEvent, entity: FallingBlockEntity) {

    }


    class FallingBlockEntity : PylonEntity<FallingBlock> {
        val blockSchema: PylonBlockSchema
        val blockData: PersistentDataContainer

        constructor(blockSchema: PylonBlockSchema, blockData: PersistentDataContainer, entity: FallingBlock) : super(KEY, entity) {
            this.blockSchema = blockSchema
            this.blockData = blockData
        }

        constructor(entity: FallingBlock) : super(entity) {
            val pdc = entity.persistentDataContainer

            val fallingBlockType = pdc.get(FALLING_BLOCK_TYPE, NamespacedKeyPersistentDataType)!!
            this.blockSchema = PylonRegistry.BLOCKS[fallingBlockType]!!
            this.blockData = pdc.get(FALLING_BLOCK_DATA, PersistentDataType.TAG_CONTAINER)!!
        }

        fun block(block: Block): PylonBlock {
            return blockSchema.load(block, blockData)
        }

        override fun write(pdc: PersistentDataContainer) {
            pdc.set(FALLING_BLOCK_TYPE, NamespacedKeyPersistentDataType, blockSchema.key)
            pdc.set(FALLING_BLOCK_DATA, PersistentDataType.TAG_CONTAINER, blockData)
        }
    }

    companion object {
        @JvmField
        val KEY = NamespacedKey(PylonCore, "falling_pylon_block")

        @JvmField
        val FALLING_BLOCK_DATA = NamespacedKey(PylonCore, "falling_pylon_block_data")

        @JvmField
        val FALLING_BLOCK_TYPE = NamespacedKey(PylonCore, "falling_pylon_block_type")
    }
}
