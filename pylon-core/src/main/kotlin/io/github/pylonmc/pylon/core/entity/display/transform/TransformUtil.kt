package io.github.pylonmc.pylon.core.entity.display.transform

import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.joml.Vector3d
import org.joml.Vector3f
import kotlin.math.roundToInt

@Suppress("unused")
object TransformUtil {

    val AXIS = listOf(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST)

    fun yawToCardinalDirection(yaw: Double): Double
        = -(yaw / 90.0F).roundToInt() * (Math.PI/2)
    fun yawToCardinalDirection(yaw: Float): Float
        = yawToCardinalDirection(yaw.toDouble()).toFloat()

    fun yawToFace(yaw: Double): BlockFace
        = AXIS[(yaw / 90.0F).roundToInt() and 0x3]
    fun yawToFace(yaw: Float): BlockFace
        = yawToFace(yaw.toDouble())

    fun rotatedRadius(radius: Float, x: Float, y: Float, z: Float): Vector3f
        = Vector3f(0.0F, 0.0F, radius).rotateX(x).rotateY(y).rotateZ(z)
    fun rotatedRadius(radius: Double, x: Double, y: Double, z: Double): Vector3d
        = Vector3d(0.0, 0.0, radius).rotateX(x).rotateY(y).rotateZ(z)
    fun rotatedRadius(radius: Float, y: Float): Vector3f
        = Vector3f(0.0F, 0.0F, radius).rotateY(y)
    fun rotatedRadius(radius: Double, y: Double): Vector3d
        = Vector3d(0.0, 0.0, radius).rotateY(y)

    fun getDisplacement(from: Location, to: Location): Vector3f
        = to.clone().subtract(from).toVector().toVector3f()
    fun getDisplacement(from: Vector3f, to: Vector3f): Vector3f
        = Vector3f(to).sub(from)
    fun getDisplacement(from: Vector3d, to: Vector3d): Vector3d
        = Vector3d(to).sub(from)

    fun getDirection(from: Location, to: Location): Vector3f
        = getDisplacement(from, to).normalize()
    fun getDirection(from: Vector3f, to: Vector3f): Vector3f
        = getDisplacement(from, to).normalize()
    fun getDirection(from: Vector3d, to: Vector3d): Vector3d
        = getDisplacement(from, to).normalize()
}