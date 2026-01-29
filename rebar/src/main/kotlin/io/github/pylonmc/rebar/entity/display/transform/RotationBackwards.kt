package io.github.pylonmc.rebar.entity.display.transform

import org.joml.*

open class RotationBackwards private constructor(
    protected val vector: Vector3f?,
    protected val quaternion: Quaternionf?
) : TransformComponent {

    constructor(rotation: Vector3f): this(rotation, null)

    constructor(rotation: Vector3d): this(Vector3f(rotation), null)

    constructor(x: Float, y: Float, z: Float): this(Vector3f(x, y, z))

    constructor(x: Double, y: Double, z: Double): this(Vector3d(x, y, z))

    constructor(rotation: Quaterniond): this(null, Quaternionf(rotation))

    constructor(rotation: Quaternionf): this(null, rotation)

    override fun apply(matrix: Matrix4f) {
        if (vector != null) {
            matrix.mul(Matrix4f().rotateXYZ(Vector3f(vector).mul(-1.0F)))
        } else if (quaternion != null) {
            matrix.mul(Matrix4f().rotate(Quaternionf(quaternion).invert()))
        } else {
            throw IllegalStateException()
        }
    }
}