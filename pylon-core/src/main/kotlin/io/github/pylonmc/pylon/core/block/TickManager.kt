package io.github.pylonmc.pylon.core.block

import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.github.shynixn.mccoroutine.bukkit.ticks
import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.block.BlockListener.logEventHandleErr
import io.github.pylonmc.pylon.core.block.base.PylonTickingBlock
import io.github.pylonmc.pylon.core.event.PylonBlockBreakEvent
import io.github.pylonmc.pylon.core.event.PylonBlockLoadEvent
import io.github.pylonmc.pylon.core.event.PylonBlockPlaceEvent
import io.github.pylonmc.pylon.core.event.PylonBlockUnloadEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import java.util.concurrent.ConcurrentHashMap

object TickManager : Listener {

    private val tickingBlocks: MutableMap<PylonBlock, Job> = ConcurrentHashMap()

    @JvmStatic
    fun isTicking(block: PylonBlock): Boolean {
        return tickingBlocks[block]?.isActive == true
    }

    @JvmSynthetic
    internal fun stopTicking(block: PylonBlock) {
        tickingBlocks.remove(block)?.cancel()
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPylonBlockPlace(e: PylonBlockPlaceEvent) {
        startTicker(e.pylonBlock)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPylonBlockBreak(e: PylonBlockBreakEvent) {
        val pylonBlock = e.pylonBlock
        tickingBlocks.remove(pylonBlock)?.cancel()
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPylonBlockLoad(e: PylonBlockLoadEvent) {
        startTicker(e.pylonBlock)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPylonBlockUnload(e: PylonBlockUnloadEvent) {
        tickingBlocks.remove(e.pylonBlock)?.cancel()
    }

    private fun startTicker(pylonBlock: PylonBlock) {
        if (pylonBlock is PylonTickingBlock) {
            val dispatcher =
                if (pylonBlock.isAsync) PylonCore.asyncDispatcher else PylonCore.minecraftDispatcher
            val tickDelay = pylonBlock.tickInterval
            tickingBlocks[pylonBlock] = PylonCore.launch(dispatcher) {
                var lastTickNanos = System.nanoTime()
                while (true) {
                    delay(tickDelay.ticks)
                    try {
                        val dt = (System.nanoTime() - lastTickNanos) / 1.0e9
                        lastTickNanos = System.nanoTime()
                        pylonBlock.tick(dt)
                    } catch (e: Exception) {
                        PylonCore.launch(PylonCore.minecraftDispatcher) {
                            logEventHandleErr(null, e, pylonBlock)
                        }
                    }
                }
            }
        }
    }
}