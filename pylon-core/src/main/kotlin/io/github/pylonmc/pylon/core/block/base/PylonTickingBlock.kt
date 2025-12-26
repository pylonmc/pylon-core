package io.github.pylonmc.pylon.core.block.base

import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.github.shynixn.mccoroutine.bukkit.ticks
import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.block.BlockListener.logEventHandleErr
import io.github.pylonmc.pylon.core.block.PylonBlock
import io.github.pylonmc.pylon.core.config.PylonConfig
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.event.PylonBlockBreakEvent
import io.github.pylonmc.pylon.core.event.PylonBlockDeserializeEvent
import io.github.pylonmc.pylon.core.event.PylonBlockLoadEvent
import io.github.pylonmc.pylon.core.event.PylonBlockPlaceEvent
import io.github.pylonmc.pylon.core.event.PylonBlockSerializeEvent
import io.github.pylonmc.pylon.core.event.PylonBlockUnloadEvent
import io.github.pylonmc.pylon.core.util.pylonKey
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.jetbrains.annotations.ApiStatus
import java.util.IdentityHashMap

/**
 * Represents a block that 'ticks' (does something at a fixed time interval).
 */
interface PylonTickingBlock {

    private val tickingData: TickingBlockData
        get() = tickingBlocks.getOrPut(this) { TickingBlockData(
            PylonConfig.defaultTickInterval,
            false,
            null
        )}

    /**
     * The interval at which the [tick] function is called. You should generally use [setTickInterval]
     * in your place constructor instead of overriding this.
     */
    val tickInterval
        get() = tickingData.tickInterval

    /**
     * Whether the [tick] function should be called asynchronously. You should generally use
     * [setAsync] in your place constructor instead of overriding this.
     */
    val isAsync
        get() = tickingData.isAsync

    /**
     * Sets how often the [tick] function should be called (in Minecraft ticks)
     */
    fun setTickInterval(tickInterval: Int) {
        tickingData.tickInterval = tickInterval
    }

    /**
     * Sets whether the [tick] function should be called asynchronously.
     *
     * WARNING: Settings a block to tick asynchronously could have unintended consequences.
     *
     * Only set this option if you understand what 'asynchronous' means, and note that you
     * cannot interact with the world asynchronously.
     */
    fun setAsync(isAsync: Boolean) {
        tickingData.isAsync = isAsync
    }

    /**
     * The function that should be called periodically.
     */
    fun tick()

    @ApiStatus.Internal
    companion object : Listener {

        internal data class TickingBlockData(
            var tickInterval: Int,
            var isAsync: Boolean,
            var job: Job?,
        )

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
                tickingBlocks.remove(block)?.job?.cancel()
            }
        }

        @EventHandler
        private fun onBreak(event: PylonBlockBreakEvent) {
            val block = event.pylonBlock
            if (block is PylonTickingBlock) {
                tickingBlocks.remove(block)?.job?.cancel()
            }
        }

        @EventHandler
        private fun onPylonBlockPlace(event: PylonBlockPlaceEvent) {
            val block = event.pylonBlock
            if (block is PylonTickingBlock) {
                startTicker(block)
            }
        }

        @EventHandler
        private fun onPylonBlockLoad(event: PylonBlockLoadEvent) {
            val block = event.pylonBlock
            if (block is PylonTickingBlock) {
                startTicker(block)
            }
        }

        /**
         * Returns true if the block is still ticking, or false if the block does
         * not exist, is not a ticking block, or has errored and been unloaded.
         */
        @JvmStatic
        @ApiStatus.Internal
        fun isTicking(block: PylonBlock?): Boolean {
            return block is PylonTickingBlock && tickingBlocks[block]?.job?.isActive == true
        }

        @JvmSynthetic
        internal fun stopTicking(block: PylonTickingBlock) {
            tickingBlocks[block]?.job?.cancel()
        }

        private fun startTicker(tickingBlock: PylonTickingBlock) {
            val dispatcher = if (tickingBlock.isAsync) PylonCore.asyncDispatcher else PylonCore.minecraftDispatcher
            tickingBlocks[tickingBlock]?.job = PylonCore.launch(dispatcher) {
                while (true) {
                    delay(tickingBlock.tickInterval.ticks)
                    try {
                        tickingBlock.tick()
                    } catch (e: Exception) {
                        PylonCore.launch(PylonCore.minecraftDispatcher) {
                            logEventHandleErr(null, e, tickingBlock as PylonBlock)
                        }
                    }
                }
            }
        }
    }
}