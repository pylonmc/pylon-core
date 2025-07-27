package io.github.pylonmc.pylon.core.block.base

import io.github.pylonmc.pylon.core.config.PylonConfig
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.event.PylonBlockDeserializeEvent
import io.github.pylonmc.pylon.core.event.PylonBlockSerializeEvent
import io.github.pylonmc.pylon.core.event.PylonBlockUnloadEvent
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.jetbrains.annotations.ApiStatus
import java.util.*

interface PylonTickingBlock {

    @get:ApiStatus.NonExtendable
    val tickingData: TickingBlockData
        get() = tickingBlocks.getOrPut(this) { TickingBlockData(
            PylonConfig.defaultTickInterval,
            false
        )}

    val tickInterval
        get() = tickingData.tickInterval

    val isAsync
        get() = tickingData.isAsync

    fun setTickInterval(tickInterval: Int) {
        tickingData.tickInterval = tickInterval
    }

    fun setAsync(isAsync: Boolean) {
        tickingData.isAsync = isAsync
    }

    fun tick(deltaSeconds: Double)

    @ApiStatus.Internal
    data class TickingBlockData(
        var tickInterval: Int,
        var isAsync: Boolean,
    )

    companion object : Listener {

        private val tickingBlockKey = pylonKey("ticking_block_data")

        private val tickingBlocks = IdentityHashMap<PylonTickingBlock, TickingBlockData>()

        @EventHandler
        private fun onDeserialize(event: PylonBlockDeserializeEvent) {
            val block = event.pylonBlock
            if (block is PylonTickingBlock) {
                tickingBlocks[block] = event.pdc.get(tickingBlockKey, PylonSerializers.TICKING_BLOCK_DATA)
                    ?: error("Ticking block data not found for ${block.key}")
            }
        }

        @EventHandler
        private fun onSerialize(event: PylonBlockSerializeEvent) {
            val block = event.pylonBlock
            if (block is PylonTickingBlock) {
                event.pdc.set(tickingBlockKey, PylonSerializers.TICKING_BLOCK_DATA, tickingBlocks[block]!!)
            }
        }

        @EventHandler
        private fun onUnload(event: PylonBlockUnloadEvent) {
            val block = event.pylonBlock
            if (block is PylonTickingBlock) {
                tickingBlocks.remove(block)
            }
        }
    }
}