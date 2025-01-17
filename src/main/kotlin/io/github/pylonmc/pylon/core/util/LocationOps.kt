package io.github.pylonmc.pylon.core.util

import org.bukkit.Location

operator fun Location.component1(): Double = x
operator fun Location.component2(): Double = y
operator fun Location.component3(): Double = z
operator fun Location.component4(): Float = yaw
operator fun Location.component5(): Float = pitch

operator fun Location.plus(other: Location): Location = clone().add(other)
operator fun Location.minus(other: Location): Location = clone().subtract(other)
operator fun Location.times(other: Double): Location = clone().multiply(other)
operator fun Location.div(other: Double): Location = clone().multiply(1 / other)