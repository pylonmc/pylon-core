package io.github.pylonmc.pylon.core.entity.display.transform

import org.joml.Vector3d
import org.joml.Vector3f

/**
 * Creates a transformation that represents a line between two points.
 *
 * You must specify [from], [to], and [thickness]; other fields are optional.
 */
open class LineBuilder {

    protected var translation: Vector3f = Vector3f()
    protected var from: Vector3f? = null
    protected var to: Vector3f? = null
    protected var thickness: Float? = null
    protected var extraLength: Float = 0.0F
    protected var roll: Float = 0.0F

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

    open fun build(): TransformBuilder {
        if (from == null || to == null || thickness == null) {
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
