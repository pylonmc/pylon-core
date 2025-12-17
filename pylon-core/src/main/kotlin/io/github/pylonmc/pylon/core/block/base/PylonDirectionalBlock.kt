package io.github.pylonmc.pylon.core.block.base

import io.github.pylonmc.pylon.core.block.PylonBlock
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.event.PylonBlockDeserializeEvent
import io.github.pylonmc.pylon.core.event.PylonBlockSerializeEvent
import io.github.pylonmc.pylon.core.event.PylonBlockUnloadEvent
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.block.BlockFace
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.jetbrains.annotations.ApiStatus
import java.util.IdentityHashMap
import kotlin.collections.set

/**
 * Represents a block that has a specific facing direction.
 *
 * Internally only used for rotating [PylonBlock.blockTextureEntity]s.
 */
interface PylonDirectionalBlock {

    var facing: BlockFace
        get() = directionalBlocks[this] ?: error("No direction was set")
        set(value) {
            directionalBlocks[this] = value
        }

    @ApiStatus.Internal
    companion object : Listener {
        private val directionalBlockKey = pylonKey("directional_block")

        private val directionalBlocks = IdentityHashMap<PylonDirectionalBlock, BlockFace>()

        @EventHandler
        private fun onDeserialize(event: PylonBlockDeserializeEvent) {
            val block = event.pylonBlock
            if (block is PylonDirectionalBlock) {
                directionalBlocks[block] = event.pdc.get(directionalBlockKey, PylonSerializers.BLOCK_FACE)
                    ?: error("Direction not found for ${block.key}")
            }
        }

        @EventHandler
        private fun onSerialize(event: PylonBlockSerializeEvent) {
            val block = event.pylonBlock
            if (block is PylonDirectionalBlock) {
                event.pdc.set(directionalBlockKey, PylonSerializers.BLOCK_FACE, directionalBlocks[block]!!)
            }
        }

        @EventHandler
        private fun onUnload(event: PylonBlockUnloadEvent) {
            val block = event.pylonBlock
            if (block is PylonDirectionalBlock) {
                directionalBlocks.remove(block)
            }
        }
    }
}