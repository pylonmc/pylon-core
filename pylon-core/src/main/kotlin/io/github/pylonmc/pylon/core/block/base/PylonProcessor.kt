package io.github.pylonmc.pylon.core.block.base

import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.event.PylonBlockDeserializeEvent
import io.github.pylonmc.pylon.core.event.PylonBlockLoadEvent
import io.github.pylonmc.pylon.core.event.PylonBlockSerializeEvent
import io.github.pylonmc.pylon.core.event.PylonBlockUnloadEvent
import io.github.pylonmc.pylon.core.util.gui.ProgressItem
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.jetbrains.annotations.ApiStatus
import java.util.IdentityHashMap

/**
 * An interface that tracks progress of some kind of process, such as processing a
 * recipe, burning a piece of fuel, enchanting an item, etc
 *
 * You can set a progress item with `setRecipeProgressItem`. This item
 * will be automatically synchronized to the process progress, and will
 * be persisted.
 *
 * @see PylonRecipeProcessor
 */
interface PylonProcessor {

    @ApiStatus.Internal
    data class ProcessorData(
        var processTimeTicks: Int?,
        var processTicksRemaining: Int?,
        var progressItem: ProgressItem?,
    )
    private val processorData: ProcessorData
        get() = processorBlocks.getOrPut(this) { ProcessorData(null, null, null)}

    val processTimeTicks: Int?
        @ApiStatus.NonExtendable
        get() = processorData.processTimeTicks

    val processTicksRemaining: Int?
        @ApiStatus.NonExtendable
        get() = processorData.processTicksRemaining

    val isProcessing: Boolean
        @ApiStatus.NonExtendable
        get() = processTimeTicks != null

    var processProgressItem: ProgressItem
        get() = processorData.progressItem ?: error("No recipe progress item was set")
        set(progressItem) {
            processorData.progressItem = progressItem
        }

    /**
     * Starts a new process with duration [ticks]
     */
    fun startProcess(ticks: Int) {
        processorData.processTimeTicks = ticks
        processorData.processTicksRemaining = ticks
        processorData.progressItem?.setTotalTimeTicks(ticks)
        processorData.progressItem?.setRemainingTimeTicks(ticks)
    }

    fun stopProcess() {
        val data = processorData
        data.processTimeTicks = null
        data.processTicksRemaining = null
        data.progressItem?.totalTime = null
    }

    fun finishProcess() {
        check(isProcessing) {
            "Cannot finish process because there is no process ongoing"
        }
        stopProcess()
        onProcessFinished()
    }

    fun onProcessFinished() {}

    /**
     * Progresses the progress by [ticks] ticks
     */
    @ApiStatus.Internal
    fun progressProcess(ticks: Int) {
        val data = processorData
        if (data.processTimeTicks == null) {
            return
        }

        data.processTicksRemaining = data.processTicksRemaining!! - ticks
        data.progressItem?.setRemainingTimeTicks(data.processTicksRemaining!!)
        if (data.processTicksRemaining!! <= 0) {
            finishProcess()
        }
    }

    @ApiStatus.Internal
    companion object : Listener {

        private val processorKey = pylonKey("processor_data")

        private val processorBlocks = IdentityHashMap<PylonProcessor, ProcessorData>()

        @EventHandler
        private fun onDeserialize(event: PylonBlockDeserializeEvent) {
            val block = event.pylonBlock
            if (block !is PylonProcessor) {
                return
            }

            val data = event.pdc.get(processorKey, PylonSerializers.PROCESSOR_DATA)
                ?: error("Processor data not found for ${block.key}")
            processorBlocks[block] = data
        }

        @EventHandler
        private fun onLoad(event: PylonBlockLoadEvent) {
            // This separate listener is needed because when [PylonBlockDeserializeEvent] fires, then the
            // block may not have been fully initialised yet (e.g. postInitialise may not have been called)
            // which means progressItem may not have been set yet
            val block = event.pylonBlock
            if (block is PylonProcessor) {
                val data = processorBlocks[block]!!
                data.progressItem?.setTotalTimeTicks(data.processTimeTicks)
                data.processTicksRemaining?.let { data.progressItem?.setRemainingTimeTicks(it) }
            }
        }

        @EventHandler
        private fun onSerialize(event: PylonBlockSerializeEvent) {
            val block = event.pylonBlock
            if (block is PylonProcessor) {
                event.pdc.set(processorKey, PylonSerializers.PROCESSOR_DATA, block.processorData)
            }
        }

        @EventHandler
        private fun onUnload(event: PylonBlockUnloadEvent) {
            val block = event.pylonBlock
            if (block is PylonProcessor) {
                processorBlocks.remove(block)
            }
        }
    }
}