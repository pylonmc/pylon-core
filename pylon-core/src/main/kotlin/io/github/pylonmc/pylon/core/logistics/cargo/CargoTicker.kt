package io.github.pylonmc.pylon.core.logistics.cargo

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.github.shynixn.mccoroutine.bukkit.ticks
import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.block.PylonBlock
import io.github.pylonmc.pylon.core.block.base.PylonCargoBlock
import io.github.pylonmc.pylon.core.config.PylonConfig
import io.github.pylonmc.pylon.core.event.PylonBlockBreakEvent
import io.github.pylonmc.pylon.core.event.PylonBlockLoadEvent
import io.github.pylonmc.pylon.core.event.PylonBlockPlaceEvent
import io.github.pylonmc.pylon.core.event.PylonBlockUnloadEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.jetbrains.annotations.ApiStatus
import java.util.concurrent.ConcurrentHashMap

@ApiStatus.Internal
object CargoTicker : Listener {

    private val tickingBlocks: MutableMap<PylonBlock, Job> = ConcurrentHashMap()

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
        if (pylonBlock !is PylonCargoBlock) {
            return
        }

        val dispatcher = PylonCore.minecraftDispatcher
        val tickDelay = PylonConfig.cargoTickInterval
        tickingBlocks[pylonBlock] = PylonCore.launch(dispatcher) {
            while (true) {
                delay(tickDelay.ticks)
                pylonBlock.tickCargo()
            }
        }
    }
}