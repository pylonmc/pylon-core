package io.github.pylonmc.pylon.core.entity.display.transform

import org.joml.Matrix4f
import org.joml.Vector3d
import org.joml.Vector3f

open class Scale(protected val scale: Vector3f) : TransformComponent {

    constructor(scale: Vector3d): this(Vector3f(scale))

    constructor(x: Float, y: Float, z: Float): this(Vector3f(x, y, z))

    constructor(x: Double, y: Double, z: Double): this(Vector3d(x, y, z))

    constructor(scale: Float): this(Vector3f(scale))

    constructor(scale: Double): this(Vector3d(scale))

    override fun apply(matrix: Matrix4f) {
        matrix.mul(Matrix4f().scale(scale))
    }
}