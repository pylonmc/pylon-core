package io.github.pylonmc.pylon.core.entity.display.transform

import org.bukkit.Location
import org.joml.Matrix4f
import org.joml.Vector3d
import org.joml.Vector3f
import kotlin.math.atan2
import kotlin.math.sqrt

open class LookAlong(direction: Vector3f) : TransformComponent {

    val angleX = -atan2(direction.y, sqrt(direction.x * direction.x + direction.z * direction.z))
    val angleY = atan2(direction.x, direction.z)

    constructor(direction: Vector3d): this(Vector3f(direction))

    constructor(from: Vector3f, to: Vector3f): this(TransformUtil.getDirection(from, to))

    constructor(from: Vector3d, to: Vector3d): this(TransformUtil.getDirection(from, to))

    constructor(from: Location, to: Location): this(TransformUtil.getDirection(from, to))

    constructor(xFrom: Float, yFrom: Float, zFrom: Float, xTo: Float, yTo: Float, zTo: Float)
            : this(Vector3f(xFrom, yFrom, zFrom), Vector3f(xTo, yTo, zTo))

    constructor(xFrom: Double, yFrom: Double, zFrom: Double, xTo: Double, yTo: Double, zTo: Double)
            : this(Vector3d(xFrom, yFrom, zFrom), Vector3d(xTo, yTo, zTo))

    override fun apply(matrix: Matrix4f) {
        matrix.mul(Matrix4f().rotateY(angleY).rotateX(angleX))
    }
}