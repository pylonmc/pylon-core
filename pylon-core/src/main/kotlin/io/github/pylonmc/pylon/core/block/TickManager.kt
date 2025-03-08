package io.github.pylonmc.pylon.core.block

import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.github.shynixn.mccoroutine.bukkit.ticks
import io.github.pylonmc.pylon.core.block.base.Ticking
import io.github.pylonmc.pylon.core.event.PylonBlockBreakEvent
import io.github.pylonmc.pylon.core.event.PylonBlockPlaceEvent
import io.github.pylonmc.pylon.core.pluginInstance
import io.github.pylonmc.pylon.core.util.position
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.bukkit.Color
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
        val block = e.block
        val pylonBlock = e.pylonBlock
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
                        // Drop onto main thread for error logging and stuff
                        withContext(pluginInstance.minecraftDispatcher) {
                            pluginInstance.logger.log(
                                SEVERE,
                                "An error occurred while ticking block ${block.position} of type ${pylonBlock.schema.key}",
                            )
                            e.printStackTrace()
                            if (++errors >= pluginInstance.allowedBlockErrors) {
                                spawnErrorBlock(pylonBlock)
                                cancel()
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private fun onPylonBlockBreak(e: PylonBlockBreakEvent) {
        val pylonBlock = e.pylonBlock
        tickingBlocks.remove(pylonBlock)?.cancel()
        pylonBlock.errorBlock?.remove()
        pylonBlock.errorBlock = null
    }

    private fun spawnErrorBlock(pylonBlock: PylonBlock<*>) {
        val block = pylonBlock.block
        val display = block.world.spawn(block.location, BlockDisplay::class.java)
        display.isInvisible = true
        display.glowColorOverride = Color.RED
        display.isGlowing = true
        pylonBlock.errorBlock = display
    }
}