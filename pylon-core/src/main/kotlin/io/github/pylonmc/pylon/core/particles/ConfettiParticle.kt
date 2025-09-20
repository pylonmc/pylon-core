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

class ConfettiParticle {
    private val display: BlockDisplay
    private var age = 0

    private val velocity: Vector
    private val angularVelocity: Vector

    private var rotationX = 0.0
    private var rotationY = 0.0
    private var rotationZ = 0.0

    constructor(location: Location, material: Material, velocity: Vector) {
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
        this.velocity = velocity

        // Random angular velocity (degrees per tick)
        this.angularVelocity = Vector(
            (RANDOM.nextDouble() - 0.5) * 7 * TICK_AMOUNT,
            (RANDOM.nextDouble() - 0.5) * 7 * TICK_AMOUNT,
            (RANDOM.nextDouble() - 0.5) * 7 * TICK_AMOUNT
        )

        startTickLoop()
    }

     constructor(location: Location, material: Material) : this(location, material, Vector(
        (RANDOM.nextDouble() - 0.5) * 0.2,
        RANDOM.nextDouble() * 0.2,
        (RANDOM.nextDouble() - 0.5) * 0.2
    ).normalize())

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
                val driftX: Double = (RANDOM.nextDouble() - 0.5) * 0.03 * TICK_AMOUNT
                val driftZ: Double = (RANDOM.nextDouble() - 0.5) * 0.03 * TICK_AMOUNT
                velocity.add(Vector(driftX, 0.0, driftZ))

                velocity.setY(velocity.getY() + GRAVITY * TICK_AMOUNT)

                velocity.multiply(DRAG)

                val loc = display.location.add(velocity)
                display.teleport(loc)

                rotationX += angularVelocity.getX() * TICK_AMOUNT
                rotationY += angularVelocity.getY() * TICK_AMOUNT
                rotationZ += angularVelocity.getZ() * TICK_AMOUNT

                val leftRotation = Quaternionf().rotationXYZ(
                    Math.toRadians(rotationX).toFloat(),
                    Math.toRadians(rotationY).toFloat(),
                    Math.toRadians(rotationZ).toFloat()
                )

                val t = display.transformation
                display.interpolationDuration = TICK_AMOUNT.toInt()
                display.transformation = Transformation(
                    t.translation,
                    leftRotation,
                    t.scale,
                    Quaternionf()
                )
            }
        }.runTaskTimer(PylonCore, 1L, TICK_AMOUNT)
    }

    companion object {
        private val RANDOM = Random()
        private const val GRAVITY = -0.02
        private const val DRAG = 0.85

        private const val MAX_AGE = 300
        private const val TICK_AMOUNT = 2L


        val CONCRETES: List<Material> = Material.entries
            .filter { mat: Material -> mat.name.endsWith("CONCRETE") && !mat.isLegacy }
            .toList()

        @JvmStatic
        @JvmOverloads
        fun spawnOne(loc: Location, mat: Material = CONCRETES.random()): Runnable {
            return Runnable { ConfettiParticle(loc, mat) }
        }

        @JvmStatic
        @JvmOverloads
        fun spawnMany(loc: Location, amount: Int, materials: Collection<Material> = CONCRETES): Runnable {
            val output: MutableList<Runnable> = ArrayList<Runnable>()

            repeat(amount) { _ ->
                output.add(spawnOne(loc, materials.random()))
            }

            return Runnable { output.forEach(Runnable::run) }
        }
    }
}