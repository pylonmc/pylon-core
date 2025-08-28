package io.github.pylonmc.pylon.core.test

import com.github.shynixn.mccoroutine.bukkit.scope
import com.github.shynixn.mccoroutine.bukkit.ticks
import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.util.position.BlockPosition
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.future
import kotlinx.coroutines.launch
import org.bukkit.*
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.util.BoundingBox
import java.util.concurrent.CompletableFuture

/**
 * Represents a running game test. A game test is a special blocked-off area in the world
 * that allows you to test blocks, items, entities, and other game mechanics in a controlled environment.
 * It contains a success condition that looks at the world and determines when the test has succeeded.
 *
 * Will succeed by default.
 *
 * @see GameTestConfig
 */
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

    fun withDelay(ticks: Int, block: Runnable) {
        PylonCore.scope.launch {
            delay(ticks.ticks)
            block.run()
        }
    }

    /**
     * Returns the center position of the game test
     */
    fun position(): BlockPosition = center

    /**
     * Returns a position relative to the center of the game test
     */
    fun position(offset: BlockPosition): BlockPosition = center + offset

    /**
     * Returns a position relative to the center of the game test
     */
    fun position(x: Int, y: Int, z: Int): BlockPosition = center + BlockPosition(world, x, y, z)

    /**
     * Returns the center location of the game test
     */
    fun location(): Location = center.location

    /**
     * Returns a location relative to the center of the game test
     */
    fun location(location: Location): Location = location.clone().add(center.location)

    /**
     * Returns a location relative to the center of the game test
     */
    fun location(x: Double, y: Double, z: Double): Location = center.location.clone().add(x, y, z)

    companion object {
        @JvmSynthetic
        internal fun submit(gameTest: GameTest, delay: Int): CompletableFuture<GameTestFailException?> {
            return PylonCore.scope.future {
                val chunks = mutableSetOf<Chunk>()
                for (x in gameTest.boundingBox.minX.toInt()..gameTest.boundingBox.maxX.toInt()) {
                    for (z in gameTest.boundingBox.minZ.toInt()..gameTest.boundingBox.maxZ.toInt()) {
                        val chunk = gameTest.world.getBlockAt(x, 0, z).chunk
                        chunk.isForceLoaded = true
                        chunks.add(chunk)
                    }
                }
                delay(delay.ticks)
                var result: GameTestFailException? = null
                val ticksAtStart = Bukkit.getCurrentTick()
                try {
                    while (true) {
                        val currentTick = Bukkit.getCurrentTick()
                        if (currentTick - ticksAtStart >= gameTest.config.timeoutTicks) {
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
                } catch (e: Throwable) {
                    result = GameTestFailException(gameTest, "An exception occurred", e)
                }
                for (entity in gameTest.world.getNearbyEntities(gameTest.boundingBox)) {
                    if (entity !is Player) {
                        entity.remove()
                    }
                }
                for (x in gameTest.boundingBox.minX.toInt()..gameTest.boundingBox.maxX.toInt()) {
                    for (y in gameTest.boundingBox.minY.toInt()..gameTest.boundingBox.maxY.toInt()) {
                        for (z in gameTest.boundingBox.minZ.toInt()..gameTest.boundingBox.maxZ.toInt()) {
                            gameTest.world.getBlockAt(x, y, z).setType(Material.AIR, false)
                        }
                    }
                }
                for (chunk in chunks) {
                    chunk.isForceLoaded = false
                }
                return@future result
            }
        }

    }
}