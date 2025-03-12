package io.github.pylonmc.pylon.core.test

import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.position.BlockPosition
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.util.BoundingBox
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import kotlin.properties.Delegates

class GameTestConfig(
    private val key: NamespacedKey,
    val size: Int,
    val setUp: (GameTest) -> Unit,
    val delayTicks: Int,
    val timeoutTicks: Int,
    val positionOverride: BlockPosition?
) : Keyed {
    override fun getKey(): NamespacedKey = key

    class Builder(val key: NamespacedKey) {
        private var size by Delegates.notNull<Int>()
        private var setUp: (GameTest) -> Unit = {}
        private var delayTicks = 0
        private var timeoutTicks = 5 * 60 * 20
        private var positionOverride: BlockPosition? = null

        fun size(size: Int): Builder = apply { this.size = size }
        fun setUp(setUp: Consumer<GameTest>): Builder = apply { this.setUp = setUp::accept }
        fun delayTicks(delayTicks: Int): Builder = apply { this.delayTicks = delayTicks }
        fun timeoutTicks(timeoutTicks: Int): Builder = apply { this.timeoutTicks = timeoutTicks }
        fun positionOverride(position: BlockPosition): Builder = apply { this.positionOverride = position }

        fun build() = GameTestConfig(key, size, setUp, delayTicks, timeoutTicks, positionOverride)
    }

    fun launch(position: BlockPosition): CompletableFuture<GameTestFailException?> {
        val realPosition = positionOverride ?: position
        val boundingBox = BoundingBox(
            realPosition.x - size - 1.0,
            realPosition.y - 1.0,
            realPosition.z - size - 1.0,
            realPosition.x + size + 1.0,
            realPosition.y + size + 1.0,
            realPosition.z + size + 1.0
        )
        val gameTest = GameTest(
            this,
            realPosition.world ?: throw IllegalArgumentException("Position must have world"),
            realPosition,
            boundingBox
        )

        // x
        for (z in -size..size) {
            for (y in 0..size) {
                gameTest.position(size + 1, y, z).block.type = Material.BARRIER
                gameTest.position(-size - 1, y, z).block.type = Material.BARRIER
            }
        }

        // z
        for (x in -size..size) {
            for (y in 0..size) {
                gameTest.position(x, y, size + 1).block.type = Material.BARRIER
                gameTest.position(x, y, -size - 1).block.type = Material.BARRIER
            }
        }

        // y
        for (x in -size..size) {
            for (z in -size..size) {
                gameTest.position(x, size + 1, z).block.type = Material.BARRIER
                gameTest.position(x, -1, z).block.type = Material.BEDROCK
            }
        }

        try {
            setUp(gameTest)
        } catch (e: Throwable) {
            return CompletableFuture.completedFuture(GameTestFailException(gameTest, "Error on setup", e))
        }

        return GameTest.submit(gameTest, delayTicks)
    }

    fun register()
            = PylonRegistry.GAMETESTS.register(this)
}