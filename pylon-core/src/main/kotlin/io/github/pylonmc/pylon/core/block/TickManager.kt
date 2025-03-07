package io.github.pylonmc.pylon.core.block

import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.github.shynixn.mccoroutine.bukkit.ticks
import io.github.pylonmc.pylon.core.event.PylonBlockBreakEvent
import io.github.pylonmc.pylon.core.event.PylonBlockPlaceEvent
import io.github.pylonmc.pylon.core.persistence.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.pluginInstance
import io.github.pylonmc.pylon.core.util.position
import io.github.pylonmc.pylon.core.util.pylonKey
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.bukkit.Color
import org.bukkit.entity.BlockDisplay
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level.SEVERE

object TickManager : Listener {

    private val tickingBlocks: MutableMap<PylonBlock<PylonBlockSchema>, Job> = ConcurrentHashMap()

    private val errorBlockKey = pylonKey("error_block")

    @JvmStatic
    fun <T> hasStoppedTicker(block: T): Boolean
            where T : PylonBlock<*>, T : Ticking {
        return tickingBlocks[block]?.isCompleted != false
    }

    @EventHandler
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

    @EventHandler
    private fun onPylonBlockBreak(e: PylonBlockBreakEvent) {
        tickingBlocks.remove(e.pylonBlock)?.cancel()

        val block = e.block
        val display = block.world.getNearbyEntities(block.location, 0.1, 0.1, 0.1)
            .filterIsInstance<BlockDisplay>()
            .firstOrNull { it.persistentDataContainer.has(errorBlockKey) }
            ?: return
        val displayBlock = display.persistentDataContainer.get(errorBlockKey, PylonSerializers.BLOCK_POSITION)
        if (displayBlock == block.position) {
            display.remove()
        }
    }

    private fun spawnErrorBlock(pylonBlock: PylonBlock<*>) {
        val block = pylonBlock.block
        val display = block.world.spawn(block.location, BlockDisplay::class.java)
        display.persistentDataContainer.set(errorBlockKey, PylonSerializers.BLOCK_POSITION, block.position)
        display.isInvisible = true
        display.glowColorOverride = Color.RED
        display.isGlowing = true
    }
}