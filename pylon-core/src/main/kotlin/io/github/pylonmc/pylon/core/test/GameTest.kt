package io.github.pylonmc.pylon.core.test

import com.github.shynixn.mccoroutine.bukkit.scope
import com.github.shynixn.mccoroutine.bukkit.ticks
import io.github.pylonmc.pylon.core.block.BlockPosition
import io.github.pylonmc.pylon.core.pluginInstance
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.future
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.util.BoundingBox
import java.util.concurrent.Future

class GameTest(
    val config: GameTestConfig,
    val world: World,
    val center: BlockPosition,
    val boundingBox: BoundingBox
) {

    private var successCondition: () -> Boolean = { true }

    fun succeed() {
        successCondition = { true }
    }

    fun succeedWhen(condition: () -> Boolean) {
        successCondition = condition
    }

    @JvmOverloads
    fun fail(message: String, cause: Throwable? = null) {
        throw GameTestFailException(this, message, cause)
    }

    inline fun entityInBounds(predicate: (Entity) -> Boolean): Boolean {
        return world.getNearbyEntities(boundingBox).any(predicate)
    }

    fun offset(offset: BlockPosition): BlockPosition = center + offset
    fun offset(x: Int, y: Int, z: Int): BlockPosition = center + BlockPosition(world, x, y, z)

    companion object {
        internal val RUNNING = mutableListOf<GameTest>()

        internal fun submit(gameTest: GameTest, delay: Int): Future<GameTestFailException?> {
            RUNNING.add(gameTest)
            return pluginInstance.scope.future {
                delay(delay.ticks)
                var result: GameTestFailException? = null
                val ticksAtStart = Bukkit.getCurrentTick()
                try {
                    while (true) {
                        val currentTick = Bukkit.getCurrentTick()
                        if (currentTick - ticksAtStart >= gameTest.config.timeout) {
                            result = GameTestFailException(gameTest, "Test timed out")
                            break
                        }
                        if (gameTest.successCondition()) {
                            result = null
                            break
                        }
                        delay(1.ticks)
                    }
                } catch (e: GameTestFailException) {
                    result = e
                } catch (e: Exception) {
                    result = GameTestFailException(gameTest, "An exception occurred", e)
                }
                RUNNING.remove(gameTest)
                return@future result
            }
        }
    }
}