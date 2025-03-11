package io.github.pylonmc.pylon.core.block

import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.github.shynixn.mccoroutine.bukkit.ticks
import io.github.pylonmc.pylon.core.block.base.Ticking
import io.github.pylonmc.pylon.core.event.PylonBlockBreakEvent
import io.github.pylonmc.pylon.core.event.PylonBlockLoadEvent
import io.github.pylonmc.pylon.core.event.PylonBlockPlaceEvent
import io.github.pylonmc.pylon.core.event.PylonBlockUnloadEvent
import io.github.pylonmc.pylon.core.pluginInstance
import io.github.pylonmc.pylon.core.util.position
import kotlinx.coroutines.*
import org.bukkit.Color.RED
import org.bukkit.entity.BlockDisplay
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level.SEVERE

object TickManager : Listener {

    private val tickingBlocks: MutableMap<PylonBlock<*>, Job> = ConcurrentHashMap()

    @JvmStatic
    fun isTicking(block: PylonBlock<*>): Boolean {
        return tickingBlocks[block]?.isActive == true
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private fun onPylonBlockPlace(e: PylonBlockPlaceEvent) {
        startTicker(e.pylonBlock)
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private fun onPylonBlockBreak(e: PylonBlockBreakEvent) {
        val pylonBlock = e.pylonBlock
        tickingBlocks.remove(pylonBlock)?.cancel()
        pylonBlock.errorBlock?.remove()
        pylonBlock.errorBlock = null
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPylonBlockLoad(e: PylonBlockLoadEvent) {
        startTicker(e.pylonBlock)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPylonBlockUnload(e: PylonBlockUnloadEvent) {
        tickingBlocks.remove(e.pylonBlock)?.cancel()
    }

    private fun startTicker(pylonBlock: PylonBlock<*>) {
        if (pylonBlock is Ticking) {
            val dispatcher =
                if (pylonBlock.isAsync) pluginInstance.asyncDispatcher else pluginInstance.minecraftDispatcher
            val tickDelay = pylonBlock.getCustomTickRate(pluginInstance.tickDelay)
            tickingBlocks[pylonBlock] = pluginInstance.launch(dispatcher) {
                var errors = 0
                while (true) {
                    delay(tickDelay.ticks)
                    try {
                        pylonBlock.tick(tickDelay / 20.0)
                    } catch (e: Throwable) {
                        handleBlockError(pylonBlock, e, errors++)
                    }
                }
            }
        }
    }

    private suspend fun CoroutineScope.handleBlockError(pylonBlock: PylonBlock<*>, error: Throwable, errors: Int) {
        // Drop onto main thread for error logging and stuff
        withContext(pluginInstance.minecraftDispatcher) {
            val block = pylonBlock.block
            pluginInstance.logger.log(
                SEVERE,
                "An error occurred while ticking block ${block.position} of type ${pylonBlock.schema.key}",
            )
            error.printStackTrace()
            if (errors >= pluginInstance.allowedBlockErrors && pylonBlock.errorBlock == null) {
                val display = block.world.spawn(block.location, BlockDisplay::class.java)
                display.isInvisible = true
                display.glowColorOverride = RED
                display.isGlowing = true
                pylonBlock.errorBlock = display

                cancel()
            }
        }
    }
}