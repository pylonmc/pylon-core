package io.github.pylonmc.pylon.core.entity.display.transform

import org.joml.Matrix4f
import org.joml.Vector3d
import org.joml.Vector3f

open class Translation(protected val translation: Vector3f) : TransformComponent {

    constructor(translation: Vector3d): this(Vector3f(translation))

    constructor(x: Float, y: Float, z: Float): this(Vector3f(x, y, z))

    constructor(x: Double, y: Double, z: Double): this(Vector3d(x, y, z))

    override fun apply(matrix: Matrix4f) {
        matrix.mul(Matrix4f().translate(translation))
    }
}