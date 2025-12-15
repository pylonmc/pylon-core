package io.github.pylonmc.pylon.core.block.base

import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.block.BlockStorage
import io.github.pylonmc.pylon.core.block.PylonBlock
import io.github.pylonmc.pylon.core.block.PylonBlockSchema
import io.github.pylonmc.pylon.core.datatypes.NamespacedKeyPersistentDataType
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.entity.PylonEntity
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.position.BlockPosition
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.entity.FallingBlock
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

/**
 * Interface meant to be used for all pylon blocks affected by gravity, like sand, gravel etc.
 *
 * If you implement this interface, and the block can't fall, then the methods will never be called.
 *
 * Beware of how you modify the passed data in the entity, because of the order of serialization and deserialization
 * some modification need to be applied directly to the PDC stored in the entity, or they will be lost.
 *
 * Also at some steps, the entity or the block might not exist yet in their relative storage,
 * handle this interface with caution as it needs to handle state using internals most of the time.
 *
 * As a suggestion, don't make important blocks with lots of data affected by gravity,
 * or it might become a nightmare to apply the correct changes.
 */
interface PylonFallingBlock {
    /**
     * When calling this, the entity doesn't exist yet in [io.github.pylonmc.pylon.core.entity.EntityStorage]
     * Called after serialization
     */
    fun onFallStart(event: EntityChangeBlockEvent, spawnedEntity: PylonFallingBlockEntity)

    /**
     * When calling this, the block doesn't exist yet in [BlockStorage]
     * Called after deserialization
     * Cancelling the event at this step does nothing, and the entity is about to be removed
     */
    fun onFallStop(event: EntityChangeBlockEvent, entity: PylonFallingBlockEntity)

    class PylonFallingBlockEntity : PylonEntity<FallingBlock> {
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
            this.blockData = pdc.get(FALLING_BLOCK_DATA, PylonSerializers.TAG_CONTAINER)!!
            this.fallingStart = pdc.get(FALLING_BLOCK_START, PylonSerializers.BLOCK_POSITION)!!
        }

        fun block(block: Block): PylonBlock {
            return blockSchema.load(block, blockData)
        }

        override fun write(pdc: PersistentDataContainer) {
            pdc.set(FALLING_BLOCK_TYPE, NamespacedKeyPersistentDataType, blockSchema.key)
            pdc.set(FALLING_BLOCK_DATA, PylonSerializers.TAG_CONTAINER, blockData)
            pdc.set(FALLING_BLOCK_START, PylonSerializers.BLOCK_POSITION, fallingStart)
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
