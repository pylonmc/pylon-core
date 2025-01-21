package io.github.pylonmc.pylon.core.test

import io.github.pylonmc.pylon.core.block.BlockPosition
import io.github.pylonmc.pylon.core.block.block
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.World
import java.util.concurrent.Future
import kotlin.properties.Delegates

data class GameTestConfig(
    val key: NamespacedKey,
    val size: Int,
    val setUp: (GameTest) -> Unit,
    val delay: Int,
    val timeout: Int,
    val canRunInParallel: Boolean
) : Keyed {
    override fun getKey(): NamespacedKey = key

    class Builder(val key: NamespacedKey) {
        private var size by Delegates.notNull<Int>()
        private var setUp: (GameTest) -> Unit = {}
        private var delay = 0
        private var timeout by Delegates.notNull<Int>()
        private var canRunInParallel = true

        fun size(size: Int): Builder = apply { this.size = size }
        fun setUp(setUp: (GameTest) -> Unit): Builder = apply { this.setUp = setUp }
        fun delay(delay: Int): Builder = apply { this.delay = delay }
        fun timeout(timeout: Int): Builder = apply { this.timeout = timeout }
        fun canRunInParallel(canRunInParallel: Boolean): Builder = apply { this.canRunInParallel = canRunInParallel }

        fun build() = GameTestConfig(key, size, setUp, delay, timeout, canRunInParallel)
    }

    fun launch(world: World): Future<Boolean> {
        val furthestExisting = GameTest.RUNNING.maxOfOrNull { it.center.x + it.config.size } ?: 0
        val newPos = BlockPosition(world, furthestExisting + size + 5, 0, 0)
        val gameTest = GameTest(this, world, newPos)

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

        return GameTest.submit(gameTest)
    }
}