package io.github.pylonmc.pylon.core.particles

import io.github.pylonmc.pylon.core.PylonCore
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.BlockDisplay
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Transformation
import org.bukkit.util.Vector
import org.joml.AxisAngle4f
import org.joml.Quaternionf
import org.joml.Vector3f
import java.util.*
import java.util.function.Consumer

class ConfettiParticle private constructor(location: Location, material: Material) {
    private val display: BlockDisplay
    private var age = 0

    private val velocity: Vector
    private val angularVelocity: Vector

    private var rotationX = 0.0
    private var rotationY = 0.0
    private var rotationZ = 0.0

    init {
        val world = location.getWorld()

        this.display = world.spawn<BlockDisplay>(location, BlockDisplay::class.java, Consumer { d: BlockDisplay ->
            d.block = material.createBlockData()
            d.transformation = Transformation(
                Vector3f(0f, 0f, 0f),
                AxisAngle4f(),
                Vector3f(0.2f, 0.01f, 0.2f),
                AxisAngle4f()
            )
        })

        this.display.teleportDuration = TICK_AMOUNT.toInt()

        // Random initial velocity
        this.velocity = Vector(
            (random.nextDouble() - 0.5) * 0.2,
            random.nextDouble() * 0.2,
            (random.nextDouble() - 0.5) * 0.2
        ).normalize()

        // Random angular velocity (degrees per tick)
        this.angularVelocity = Vector(
            (random.nextDouble() - 0.5) * 7,
            (random.nextDouble() - 0.5) * 7,
            (random.nextDouble() - 0.5) * 7
        )

        startTickLoop()
    }

    private fun startTickLoop() {
        object : BukkitRunnable() {
            override fun run() {
                if (age++ > MAX_AGE || display.isDead) {
                    display.remove()
                    cancel()
                    return
                }

                if (!display.location.block.isEmpty) {
                    display.remove()
                    cancel()
                    return
                }

                // Apply pseudo-random drift
                val driftX: Double = (random.nextDouble() - 0.5) * 0.03
                val driftZ: Double = (random.nextDouble() - 0.5) * 0.03
                velocity.add(Vector(driftX, 0.0, driftZ))

                velocity.setY(velocity.getY() + GRAVITY)

                velocity.multiply(DRAG)

                val loc = display.location.add(velocity)
                display.teleport(loc)

                rotationX += angularVelocity.getX()
                rotationY += angularVelocity.getY()
                rotationZ += angularVelocity.getZ()

                val leftRotation = Quaternionf().rotationXYZ(
                    Math.toRadians(rotationX).toFloat(),
                    Math.toRadians(rotationY).toFloat(),
                    Math.toRadians(rotationZ).toFloat()
                )

                val t = display.transformation
                display.transformation = Transformation(
                    t.translation,
                    leftRotation,
                    t.scale,
                    Quaternionf()
                )
            }
        }.runTaskTimer(PylonCore, 1L, TICK_AMOUNT)
    }

    /**
     * Class with util methods that returns a runnable that once ran spawns all wanted particles
     */
    object Factory {
        private val WOOLS: List<Material> = Material.entries
            .filter { mat: Material -> mat.name.endsWith("CONCRETE") && !mat.isLegacy }
            .toList()

        @JvmStatic
        fun single(loc: Location, mat: Material): Runnable {
            return Runnable { ConfettiParticle(loc, mat) }
        }

        @JvmStatic
        fun many(loc: Location, materials: List<Material?>, amount: Int): Runnable {
            val output: MutableList<Runnable> = ArrayList<Runnable>()

            (0..<amount).forEach { _ ->
                val rnd: Int = random.nextInt(0, materials.size)
                output.add(single(loc, materials[rnd]!!))
            }

            return Runnable { output.forEach(Runnable::run) }
        }

        @JvmStatic
        fun many(loc: Location, amount: Int): Runnable {
            return many(loc, WOOLS, amount)
        }
    }

    companion object {
        private val random = Random()
        private const val GRAVITY = -0.015
        private const val DRAG = 0.85

        private const val MAX_AGE = 300
        private const val TICK_AMOUNT = 2L
    }
}