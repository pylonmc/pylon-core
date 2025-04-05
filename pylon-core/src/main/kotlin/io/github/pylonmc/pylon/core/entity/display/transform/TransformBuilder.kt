package io.github.pylonmc.pylon.core.entity.display.transform

import org.bukkit.Location
import org.joml.*

@Suppress("unused")
class TransformBuilder(val components: ArrayDeque<TransformComponent>) {

    constructor(): this(ArrayDeque())

    constructor(other: TransformBuilder): this(other.components)

    fun add(component: TransformComponent) = apply {
        components.addLast(component)
    }

    fun translate(translation: Vector3f) = apply { add(Translation(translation)) }
    fun translate(translation: Vector3d) = apply { add(Translation(translation)) }
    fun translate(x: Float, y: Float, z: Float) = apply { add(Translation(x, y, z)) }
    fun translate(x: Double, y: Double, z: Double) = apply { add(Translation(x, y, z)) }

    fun scale(scale: Vector3f) = apply { add(Scale(scale)) }
    fun scale(scale: Vector3d) = apply { add(Scale(scale)) }
    fun scale(x: Float, y: Float, z: Float) = apply { add(Scale(x, y, z)) }
    fun scale(x: Double, y: Double, z: Double) = apply { add(Scale(x, y, z)) }
    fun scale(scale: Float) = apply { add(Scale(scale)) }
    fun scale(scale: Double) = apply { add(Scale(scale)) }

    fun rotate(rotation: Vector3f) = apply { add(Rotation(rotation)) }
    fun rotate(rotation: Vector3d) = apply { add(Rotation(rotation)) }
    fun rotate(rotation: Quaternionf) = apply { add(Rotation(rotation)) }
    fun rotate(rotation: Quaterniond) = apply { add(Rotation(rotation)) }
    fun rotate(x: Float, y: Float, z: Float) = apply { add(Rotation(x, y, z)) }
    fun rotate(x: Double, y: Double, z: Double) = apply { add(Rotation(x, y, z)) }

    fun rotateBackwards(rotation: Vector3f) = apply { add(RotationBackwards(rotation)) }
    fun rotateBackwards(rotation: Vector3d) = apply { add(RotationBackwards(rotation)) }
    fun rotateBackwards(rotation: Quaternionf) = apply { add(RotationBackwards(rotation)) }
    fun rotateBackwards(rotation: Quaterniond) = apply { add(RotationBackwards(rotation)) }
    fun rotateBackwards(x: Float, y: Float, z: Float) = apply { add(RotationBackwards(x, y, z)) }
    fun rotateBackwards(x: Double, y: Double, z: Double) = apply { add(RotationBackwards(x, y, z)) }

    fun lookAlong(direction: Vector3f) = apply { add(LookAlong(direction)) }
    fun lookAlong(direction: Vector3d) = apply { add(LookAlong(direction)) }
    fun lookAlong(from: Vector3f, to: Vector3f) = apply { add(LookAlong(from, to)) }
    fun lookAlong(from: Vector3d, to: Vector3d) = apply { add(LookAlong(from, to)) }
    fun lookAlong(from: Location, to: Location) = apply { add(LookAlong(from, to)) }
    fun lookAlong(xFrom: Float, yFrom: Float, zFrom: Float, xTo: Float, yTo: Float, zTo: Float)
        = apply { add(LookAlong(xFrom, yFrom, zFrom, xTo, yTo, zTo)) }
    fun lookAlong(xFrom: Double, yFrom: Double, zFrom: Double, xTo: Double, yTo: Double, zTo: Double)
        = apply { add(LookAlong(xFrom, yFrom, zFrom, xTo, yTo, zTo)) }

    fun build(): Matrix4f {
        val matrix = Matrix4f()
        while (!components.isEmpty()) {
            components.removeFirst().apply(matrix)
        }
        return matrix
    }

    /**
     * Adjusts the transformation so that the transformation acts on the center of the block
     * display; otherwise it would act on a corner
     */
    fun buildForBlockDisplay(): Matrix4f {
        val cloned = TransformBuilder(this)
        cloned.add(Translation(BLOCK_DISPLAY_ADJUSTMENT))
        return build()
    }

    fun buildForItemDisplay(): Matrix4f {
        return TransformBuilder(this).build()
    }

    fun buildForTextDisplay(): Matrix4f {
        return TransformBuilder(this).build()
    }

    class LineBuilder {

        var translation: Vector3f = Vector3f()
        var from: Vector3f? = null
        var to: Vector3f? = null
        var thickness: Float? = null
        var extraLength: Float = 0.0F
        var roll: Float = 0.0F

        fun translation(translation: Vector3f) = apply { this.translation = translation }
        fun translation(translation: Vector3d) = translation(Vector3f(translation))
        fun translation(x: Float, y: Float, z: Float) = translation(Vector3f(x, y, z))
        fun translation(x: Double, y: Double, z: Double) = translation(Vector3d(x, y, z))
        fun from(from: Vector3f) = apply { this.from = from }
        fun from(from: Vector3d) = from(Vector3f(from))
        fun from(x: Float, y: Float, z: Float) = from(Vector3f(x, y, z))
        fun from(x: Double, y: Double, z: Double) = from(Vector3d(x, y, z))
        fun to(to: Vector3f) = apply { this.to = to }
        fun to(to: Vector3d) = to(Vector3f(to))
        fun to(x: Float, y: Float, z: Float) = to(Vector3f(x, y, z))
        fun to(x: Double, y: Double, z: Double) = to(Vector3d(x, y, z))
        fun thickness(thickness: Float) = apply { this.thickness = thickness }
        fun thickness(thickness: Double) = thickness(thickness.toFloat())
        fun extraLength(extraLength: Float) = apply { this.extraLength = extraLength }
        fun extraLength(extraLength: Double) = extraLength(extraLength.toFloat())
        fun roll(roll: Float) = apply { this.roll = roll }
        fun roll(roll: Double) = roll(roll.toFloat())

        fun build(): TransformBuilder {
            if ((from == null) or (to == null) or (thickness == null)) {
                throw IllegalStateException("From, to, and thickness in LineBuilder must all be specified")
            }
            val midpoint = TransformUtil.getMidpoint(from!!, to!!).add(translation)
            return TransformBuilder()
                .translate(midpoint)
                .lookAlong(from!!, to!!)
                .rotate(0.0F, 0.0F, roll)
                .scale(Vector3f(thickness!!, thickness!!, from!!.distance(to!!) + extraLength))
        }
    }

    companion object {
        private val BLOCK_DISPLAY_ADJUSTMENT: Vector3f = Vector3f(-0.5F)
    }
}