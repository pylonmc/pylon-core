package io.github.pylonmc.pylon.core.util.gui.unit

import net.kyori.adventure.text.Component

/**
 * Enum order is guaranteed to be the same as the SI unit prefixes, from largest to smallest.
 */
enum class MetricPrefix(val scale: Int) {
    QUETTA(30),
    RONNA(27),
    YOTTA(24),
    ZETTA(21),
    EXA(18),
    PETA(15),
    TERA(12),
    GIGA(9),
    MEGA(6),
    KILO(3),
    HECTO(2),
    DECA(1),
    NONE(0),
    DECI(-1),
    CENTI(-2),
    MILLI(-3),
    MICRO(-6),
    NANO(-9),
    PICO(-12),
    FEMTO(-15),
    ATTO(-18),
    ZEPTO(-21),
    YOCTO(-24),
    RONTO(-27),
    QUECTO(-30)
    ;

    val fullName: Component = Component.translatable("pylon.pyloncore.unit.prefix.${name.lowercase()}.name")
    val abbreviation: Component = Component.translatable("pylon.pyloncore.unit.prefix.${name.lowercase()}.abbr")

    /**
     * A set of prefixes that are not commonly used for their respective SI units.
     */
    object Unused {

        /**
         * Prefixed not commonly used anywhere
         */
        @JvmField
        val GENERAL = setOf(HECTO, DECA, DECI, CENTI)

        @JvmField
        val LENGTH = setOf(HECTO, DECA, DECI)

        @JvmField
        val MASS = setOf(HECTO, DECA, DECI, CENTI)

        @JvmField
        val VOLUME = setOf(HECTO, DECA, DECI, CENTI)
    }
}