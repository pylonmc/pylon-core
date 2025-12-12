package io.github.pylonmc.pylon.core.block.base

import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.block.BlockStorage
import io.github.pylonmc.pylon.core.block.PylonBlock
import io.github.pylonmc.pylon.core.block.PylonBlockSchema
import io.github.pylonmc.pylon.core.datatypes.NamespacedKeyPersistentDataType
import io.github.pylonmc.pylon.core.entity.PylonEntity
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.position.BlockPosition
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.entity.FallingBlock
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

interface PylonFallingBlock {
    /**
     * When calling this, the entity doesn't exist yet in [io.github.pylonmc.pylon.core.entity.EntityStorage]
     */
    fun onFallStart(event: EntityChangeBlockEvent, spawnedEntity: FallingBlockEntity)

    /**
     * When calling this, the block doesn't exist yet in [BlockStorage]
     */
    fun onFallStop(event: EntityChangeBlockEvent, entity: FallingBlockEntity)

    class FallingBlockEntity : PylonEntity<FallingBlock> {
        val fallingStart: BlockPosition
        val blockSchema: PylonBlockSchema
        val blockData: PersistentDataContainer

        constructor(blockSchema: PylonBlockSchema, blockData: PersistentDataContainer, fallingStart: BlockPosition, entity: FallingBlock) : super(KEY, entity) {
            this.blockSchema = blockSchema
            this.blockData = blockData
            this.fallingStart = fallingStart
        }

        constructor(entity: FallingBlock) : super(entity) {
            val pdc = entity.persistentDataContainer

            val fallingBlockType = pdc.get(FALLING_BLOCK_TYPE, NamespacedKeyPersistentDataType)!!
            this.blockSchema = PylonRegistry.BLOCKS[fallingBlockType]!!
            this.blockData = pdc.get(FALLING_BLOCK_DATA, PersistentDataType.TAG_CONTAINER)!!
            this.fallingStart = BlockPosition(entity.world, pdc.get(FALLING_BLOCK_START, PersistentDataType.LONG)!!)
        }

        fun block(block: Block): PylonBlock {
            return blockSchema.load(block, blockData)
        }

        override fun write(pdc: PersistentDataContainer) {
            pdc.set(FALLING_BLOCK_TYPE, NamespacedKeyPersistentDataType, blockSchema.key)
            pdc.set(FALLING_BLOCK_DATA, PersistentDataType.TAG_CONTAINER, blockData)
            pdc.set(FALLING_BLOCK_START, PersistentDataType.LONG, fallingStart.asLong)
        }
    }

    companion object {
        @JvmField
        val KEY = NamespacedKey(PylonCore, "falling_pylon_block")

        @JvmField
        val FALLING_BLOCK_DATA = NamespacedKey(PylonCore, "falling_pylon_block_data")

        @JvmField
        val FALLING_BLOCK_TYPE = NamespacedKey(PylonCore, "falling_pylon_block_type")

        @JvmField
        val FALLING_BLOCK_START = NamespacedKey(PylonCore, "falling_pylon_block_start")
    }
}
