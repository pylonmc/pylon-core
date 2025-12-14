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
 * This interface overrides [PylonTickingBlock.tick], meaning the rate at which the
 * block progresses is determined by [PylonTickingBlock.setTickInterval].
 */
interface PylonProcessor {

    data class ProcessorData(
        var processTimeTicks: Int?,
        var processTicksRemaining: Int?,
        var progressItem: ProgressItem?,
    )
    private val processorData: ProcessorData
        get() = processorBlocks.getOrPut(this) { ProcessorData(null, null, null)}


    val processTimeTicks: Int?
        get() = processorData.processTimeTicks

    val processTicksRemaining: Int?
        get() = processorData.processTicksRemaining

    val isProcessing: Boolean
        get() = processTimeTicks != null

     /**
     * Set the progress item that should be updated as the process progresses. Optional.
     *
     * Does not persist; you must call this whenever the block is initialised (e.g.
     * in [io.github.pylonmc.pylon.core.block.PylonBlock.postInitialise])
     */
    @ApiStatus.NonExtendable
    fun setProgressItem(item: ProgressItem) {
        processorData.progressItem = item
    }

    /**
     * Starts a new process with duration [ticks], with [ticks] being the number of server
     * ticks the process will take.
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
        onProcessFinished()
        stopProcess()
    }

    fun onProcessFinished() {}

    fun progressProcess(ticks: Int) {
        val data = processorData
        if (data.processTimeTicks != null) {
            data.processTicksRemaining = data.processTicksRemaining!! - ticks
            data.progressItem?.setRemainingTimeTicks(data.processTicksRemaining!!)
            if (data.processTicksRemaining!! <= 0) {
                finishProcess()
            }
        }
    }

    companion object : Listener {

        private val processorKey = pylonKey("processor_data")

        private val processorBlocks = IdentityHashMap<PylonProcessor, ProcessorData>()

        @EventHandler
        private fun onDeserialize(event: PylonBlockDeserializeEvent) {
            val block = event.pylonBlock
            if (block is PylonProcessor) {
                val data = event.pdc.get(processorKey, PylonSerializers.PROCESSOR_DATA)
                    ?: error("Processor data not found for ${block.key}")
                processorBlocks[block] = data
            }
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
                val data = processorBlocks[block] ?: error {
                    "No recipe processor data found for ${block.key}"
                }
                event.pdc.set(processorKey, PylonSerializers.PROCESSOR_DATA, data)
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