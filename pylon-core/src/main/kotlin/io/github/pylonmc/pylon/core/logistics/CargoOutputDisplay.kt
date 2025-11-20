package io.github.pylonmc.pylon.core.logistics

import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.entity.EntityStorage
import io.github.pylonmc.pylon.core.entity.PylonEntity
import io.github.pylonmc.pylon.core.entity.display.ItemDisplayBuilder
import io.github.pylonmc.pylon.core.entity.display.transform.TransformBuilder
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.util.position.BlockPosition
import io.github.pylonmc.pylon.core.util.pylonKey
import io.github.pylonmc.pylon.core.util.setNullable
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.ItemDisplay
import org.bukkit.persistence.PersistentDataContainer
import org.jetbrains.annotations.ApiStatus

@ApiStatus.Internal
class CargoOutputDisplay : PylonEntity<ItemDisplay> {
    val block: BlockPosition
    val face: BlockFace
    val next: BlockPosition?

    constructor(block: BlockPosition, face: BlockFace) : super(KEY, makeEntity(block.block, face)) {
        this.block = block
        this.face = face
        this.next = null

        EntityStorage.add(this)
    }

    constructor(entity: ItemDisplay) : super(entity) {
        this.block = entity.persistentDataContainer.get(BLOCK_KEY, PylonSerializers.BLOCK_POSITION)!!
        this.face = entity.persistentDataContainer.get(FACE_KEY, PylonSerializers.BLOCK_FACE)!!
        this.next = entity.persistentDataContainer.get(NEXT_KEY, PylonSerializers.BLOCK_POSITION)
    }

    override fun write(pdc: PersistentDataContainer) {
        pdc.set(BLOCK_KEY, PylonSerializers.BLOCK_POSITION, block)
        pdc.set(FACE_KEY, PylonSerializers.BLOCK_FACE, face)
        pdc.setNullable(NEXT_KEY, PylonSerializers.BLOCK_POSITION, next)
    }

    companion object {
        const val POINT_SIZE: Float = 0.2f
        const val POINT_LENGTH: Float = 0.1f

        val KEY = pylonKey("cargo_input_display")

        private val BLOCK_KEY = pylonKey("block_key")
        private val FACE_KEY = pylonKey("block_key")
        private val NEXT_KEY = pylonKey("next")

        private fun makeEntity(block: Block, face: BlockFace): ItemDisplay {
            return ItemDisplayBuilder()
                .brightness(7)
                .transformation(TransformBuilder()
                    .lookAlong(face)
                    .scale(POINT_SIZE, POINT_SIZE, POINT_LENGTH)
                )
                .itemStack(ItemStackBuilder.of(Material.RED_TERRACOTTA)
                    .addCustomModelDataString("cargo_output_display")
                )
                .build(block.location.toCenterLocation().add(face.direction.multiply(0.5)))
        }
    }
}