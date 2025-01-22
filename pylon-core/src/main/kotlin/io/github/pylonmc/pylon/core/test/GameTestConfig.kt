package io.github.pylonmc.pylon.core.test

import io.github.pylonmc.pylon.core.block.BlockPosition
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.util.BoundingBox
import java.util.concurrent.CompletableFuture
import kotlin.properties.Delegates

class GameTestConfig(
    private val key: NamespacedKey,
    val size: Int,
    val setUp: (GameTest) -> Unit,
    val delay: Int,
    val timeout: Int,
    val isParallelCapable: Boolean
) : Keyed {
    override fun getKey(): NamespacedKey = key

    class Builder(val key: NamespacedKey) {
        private var size by Delegates.notNull<Int>()
        private var setUp: (GameTest) -> Unit = {}
        private var delay = 0
        private var timeout = Int.MAX_VALUE
        private var isParallelCapable = true

        fun size(size: Int): Builder = apply { this.size = size }
        fun setUp(setUp: (GameTest) -> Unit): Builder = apply { this.setUp = setUp }
        fun delay(delay: Int): Builder = apply { this.delay = delay }
        fun timeout(timeout: Int): Builder = apply { this.timeout = timeout }
        fun isParallelCapable(isParallelCapable: Boolean): Builder =
            apply { this.isParallelCapable = isParallelCapable }

        fun build() = GameTestConfig(key, size, setUp, delay, timeout, isParallelCapable)
    }

    fun launch(world: World): CompletableFuture<GameTestFailException?> {
        val furthestExisting = GameTest.RUNNING.maxOfOrNull { it.center.x + it.config.size } ?: 0
        val newPos = BlockPosition(world, furthestExisting + size + 5, 0, 0)
        val boundingBox = BoundingBox(
            newPos.x - size - 1.0,
            newPos.y.toDouble(),
            newPos.z - size - 1.0,
            newPos.x + size + 1.0,
            newPos.y + size + 1.0,
            newPos.z + size + 1.0
        )
        val gameTest = GameTest(this, world, newPos, boundingBox)

        // x
        for (z in -size..size) {
            for (y in 0..size) {
                gameTest.offset(size + 1, y, z).block.type = Material.BARRIER
                gameTest.offset(-size - 1, y, z).block.type = Material.BARRIER
            }
        }

        // z
        for (x in -size..size) {
            for (y in 0..size) {
                gameTest.offset(x, y, size + 1).block.type = Material.BARRIER
                gameTest.offset(x, y, -size - 1).block.type = Material.BARRIER
            }
        }

        // roof
        for (x in -size..size) {
            for (z in -size..size) {
                gameTest.offset(x, size + 1, z).block.type = Material.BARRIER
            }
        }

        setUp(gameTest)

        return GameTest.submit(gameTest, delay)
    }
}