package io.github.pylonmc.pylon.core.test

import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.github.shynixn.mccoroutine.bukkit.scope
import io.github.pylonmc.pylon.core.block.BlockPosition
import io.github.pylonmc.pylon.core.pluginInstance
import kotlinx.coroutines.future.future
import kotlinx.coroutines.withContext
import org.bukkit.Bukkit
import org.bukkit.World
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Future

class GameTest(
    val config: GameTestConfig,
    val world: World,
    val center: BlockPosition
) {

    fun offset(offset: BlockPosition): BlockPosition = center + offset
    fun offset(x: Int, y: Int, z: Int): BlockPosition = center + BlockPosition(world, x, y, z)

    companion object {
        internal val RUNNING = CopyOnWriteArrayList<GameTest>()

        internal fun submit(gameTest: GameTest): Future<Boolean> {
            RUNNING.add(gameTest)
            val ticksAtStart = Bukkit.getCurrentTick()
            return pluginInstance.scope.future(pluginInstance.asyncDispatcher) {
                while (true) {
                    val currentTick = withContext(pluginInstance.minecraftDispatcher) { Bukkit.getCurrentTick() }
                    if (currentTick - ticksAtStart >= gameTest.config.timeout) {
                        RUNNING.remove(gameTest)
                        return@future false
                    }
                    // TODO finish
                }
                true
            }
        }
    }
}