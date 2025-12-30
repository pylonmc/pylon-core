package io.github.pylonmc.pylon.core.entity.base

import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.github.shynixn.mccoroutine.bukkit.ticks
import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.config.PylonConfig
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.entity.EntityListener
import io.github.pylonmc.pylon.core.entity.PylonEntity
import io.github.pylonmc.pylon.core.event.PylonEntityAddEvent
import io.github.pylonmc.pylon.core.event.PylonEntityDeathEvent
import io.github.pylonmc.pylon.core.event.PylonEntityLoadEvent
import io.github.pylonmc.pylon.core.event.PylonEntityUnloadEvent
import io.github.pylonmc.pylon.core.util.pylonKey
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.jetbrains.annotations.ApiStatus
import java.util.*

/**
 * Represents a block that 'ticks' (does something at a fixed time interval).
 */
interface PylonTickableEntity {

    private val tickingData: TickingEntityData
        get() = tickingEntities.getOrPut(this) { TickingEntityData(
            PylonConfig.DEFAULT_TICK_INTERVAL,
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

        data class TickingEntityData(
            var tickInterval: Int,
            var isAsync: Boolean,
            var job: Job?,
        )

        private val tickingEntityKey = pylonKey("ticking_entity_data")

        private val tickingEntities = IdentityHashMap<PylonTickableEntity, TickingEntityData>()

        @EventHandler
        private fun onSerialize(event: PylonEntityAddEvent) { //todo serialize
            val entity = event.pylonEntity
            if (entity is PylonTickableEntity) {
                entity.entity.persistentDataContainer.set(tickingEntityKey, PylonSerializers.TICKING_ENTITY_DATA, tickingEntities[entity]!!)
            }
        }

        @EventHandler
        private fun onUnload(event: PylonEntityUnloadEvent) {
            val entity = event.pylonEntity
            if (entity is PylonTickableEntity) {
                tickingEntities.remove(entity)?.job?.cancel()
            }
        }

        @EventHandler
        private fun onDeath(event: PylonEntityDeathEvent) {
            val entity = event.pylonEntity
            if (entity is PylonTickableEntity) {
                tickingEntities.remove(entity)?.job?.cancel()
            }
        }

        @EventHandler
        private fun onEntityLoad(event: PylonEntityLoadEvent) {
            val entity = event.pylonEntity
            if (entity is PylonTickableEntity) {
                startTicker(entity)
            }
        }

        /**
         * Returns true if the block is still ticking, or false if the block does
         * not exist, is not a ticking block, or has errored and been unloaded.
         */
        @JvmStatic
        @ApiStatus.Internal
        fun isTicking(block: PylonEntity<*>?): Boolean {
            return block is PylonTickableEntity && tickingEntities[block]?.job?.isActive == true
        }

        @JvmSynthetic
        internal fun stopTicking(block: PylonTickableEntity) {
            tickingEntities[block]?.job?.cancel()
        }

        private fun startTicker(tickingBlock: PylonTickableEntity) {
            val dispatcher = if (tickingBlock.isAsync) PylonCore.asyncDispatcher else PylonCore.minecraftDispatcher
            tickingEntities[tickingBlock]?.job = PylonCore.launch(dispatcher) {
                while (true) {
                    delay(tickingBlock.tickInterval.ticks)
                    try {
                        tickingBlock.tick()
                    } catch (e: Exception) {
                        PylonCore.launch(PylonCore.minecraftDispatcher) {
                            EntityListener.logEventHandleErr(null, e, tickingBlock as PylonEntity<*>)
                        }
                    }
                }
            }
        }
    }
}