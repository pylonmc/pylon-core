@file:Suppress("unused")

package io.github.pylonmc.rebar.util.gui.unit

import io.github.pylonmc.rebar.Rebar
import io.github.pylonmc.rebar.i18n.RebarTranslator.Companion.translator
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.time.Duration
import java.util.*

/**
 * Handles formatting of a specific unit. Call [format] to format a value using this unit.
 *
 * @param singular A component representing the long singular form of this unit (kilogram, meter, liter, etc)
 * @param plural A component representing the long plural form of this unit (kilograms, meters, liters, etc)
 * @param abbreviation A component representing the abbreviated form of this unit (kg, m, L, etc). May be null to indicate that the unit does not have an abbreviation
 * @param defaultPrefix The prefix (kilo, nano, etc) used for this unit unless specified while formatting.
 * For example, if you create a 'grams' unit and specify [MetricPrefix.KILO] as the default prefix, calling
 * [format] with 100 will return '100 kilograms'
 * @param defaultStyle The style to apply to the unit (not the value) to the output.
 */
class UnitFormat @JvmOverloads constructor(
    val singular: Component,
    val plural: Component,
    val abbreviation: Component? = null,
    val defaultPrefix: MetricPrefix = MetricPrefix.NONE,
    val defaultStyle: Style = Style.empty()
) {

    /**
     * Enables the use of this unit in the custom `<unit:[name]>` tag in [Rebar's custom MiniMessage parser][io.github.pylonmc.rebar.i18n.customMiniMessage]
     *
     * @param name the name to be used in the tag
     * @return this [UnitFormat]
     */
    fun allowUseInUnitTag(name: String) = apply { namedUnits[name] = this }

    /**
     * Returns a **new** [UnitFormat] with the same parameters as this one but with a different default prefix
     */
    fun withDefaultPrefix(prefix: MetricPrefix) = UnitFormat(singular, plural, abbreviation, prefix, defaultStyle)

    /**
     * Returns a **new** [UnitFormat] with the same parameters as this one but with a different default style
     */
    fun withDefaultStyle(style: Style) = UnitFormat(singular, plural, abbreviation, defaultPrefix, style)

    fun format(value: BigDecimal) = Formatted(value.stripTrailingZeros())

    fun format(value: Int) = format(value.toLong())

    fun format(value: Long) = format(value.toBigDecimal())

    /**
     * NaN and infinity are not supported
     */
    fun format(value: Float) = format(value.toDouble())

    /**
     * NaN and infinity are not supported
     */
    fun format(value: Double): Formatted {
        require(!value.isNaN() && !value.isInfinite()) { "Cannot format NaN or infinite values" }
        return format(value.toBigDecimal())
    }

    /**
     * Represents a value that has already been formatted.
     *
     * You can use this class to override how an already-formatted value is displayed.
     */
    inner class Formatted internal constructor(private val value: BigDecimal) : ComponentLike {
        private var sigFigs = value.precision()
        private var decimalPlaces = value.scale()
        private var forceDecimalPlaces = false
        private var abbreviate = true
        private var unitStyle = defaultStyle
        private var valueStyle = Style.empty()
        private var prefix: MetricPrefix? = defaultPrefix
        private val badPrefixes = EnumSet.noneOf(MetricPrefix::class.java)

        /**
         * Sets the number of significant figures. Uses [RoundingMode.HALF_UP] for rounding.
         * For example, if this is set to `3`, then a value of `0.472894` will be shown as `0.473`.
         */
        fun significantFigures(sigFigs: Int) = apply { this.sigFigs = sigFigs }

        /**
         * Sets the number of decimal places. This overrides significant figures if both are set.
         * If [force] is true, the formatted number will always have this many decimal places.
         * Uses [RoundingMode.HALF_UP] for rounding.
         */
        fun decimalPlaces(decimalPlaces: Int, force: Boolean) = apply {
            this.decimalPlaces = decimalPlaces
            this.forceDecimalPlaces = force
        }

        /**
         * Sets the number of decimal places. This overrides significant figures if both are set.
         * Uses [RoundingMode.HALF_UP] for rounding.
         */
        fun decimalPlaces(decimalPlaces: Int) = decimalPlaces(decimalPlaces, false)

        /**
         * Sets whether the abbreviation should be used instead of the full name.
         */
        fun abbreviate(abbreviate: Boolean) = apply { this.abbreviate = abbreviate }

        /**
         * Overrides the style of the unit.
         */
        fun unitStyle(style: Style) = apply { this.unitStyle = style }

        /**
         * Overrides the style of the value.
         */
        fun valueStyle(style: Style) = apply { this.valueStyle = style }

        /**
         * Overrides the default prefix. **This will not rescale the number like [selectPrefixAndRescale] does.**
         */
        fun prefix(prefix: MetricPrefix) = apply { this.prefix = prefix }

        /**
         * [selectPrefixAndRescale] will not use any prefixes in this collection when automatically selecting a prefix.
         */
        fun ignorePrefixes(prefixes: Collection<MetricPrefix>) = apply { badPrefixes.addAll(prefixes) }

        /**
         * [selectPrefixAndRescale] will not use any of these [prefixes] when automatically selecting a prefix.
         */
        fun ignorePrefixes(vararg prefixes: MetricPrefix) = apply { badPrefixes.addAll(prefixes) }

        /**
         * Same as [ignorePrefixes] but for [MetricPrefix.COMMONLY_UNUSED_PREFIXES]
         */
        fun ignoreCommonlyUnusedPrefixes() = ignorePrefixes(MetricPrefix.COMMONLY_UNUSED_PREFIXES)

        /**
         * Automatically selects an appropriate prefix based on the value and rescales the value accordingly.
         * **Default prefix is ignored when using this method.**
         */
        fun selectPrefixAndRescale() = apply { prefix = null }

        /**
         * Builds a component representing the value and unit.
         */
        fun build(): Component {
            var usedValue = value.round(MathContext(sigFigs, RoundingMode.HALF_UP))
            usedValue = usedValue.setScale(decimalPlaces, RoundingMode.HALF_UP)
            if (!forceDecimalPlaces) {
                usedValue = usedValue.stripTrailingZeros()
            }

            val usedPrefix = if (prefix == null) {
                val exponent = value.precision() - value.scale() - if (value.signum() == 0) 0 else 1
                val prefix = MetricPrefix.entries.firstOrNull { it.scale <= exponent && it !in badPrefixes }
                    ?: defaultPrefix
                usedValue = usedValue.movePointRight(prefix.scale)
                prefix
            } else {
                prefix!!
            }

            val number = Component.text(usedValue.toPlainString()).style(valueStyle)
            var unit = Component.empty().style(unitStyle)
            unit = if (abbreviate && abbreviation != null) {
                unit
                    .append(usedPrefix.abbreviation)
                    .append(abbreviation)
            } else {
                unit
                    .append(usedPrefix.fullName)
                    .append(if (usedValue == BigDecimal.ONE) singular else plural)
            }

            return number
                .append(Component.text(" "))
                .append(unit)
        }

        /**
         * Alias for [build]
         */
        override fun asComponent() = build()
    }

    companion object {

        @JvmSynthetic
        internal val namedUnits = mutableMapOf<String, UnitFormat>()

        private fun rebar(
            name: String,
            color: TextColor,
            prefix: MetricPrefix = MetricPrefix.NONE,
        ): UnitFormat {
            val singular = Component.translatable("rebar.unit.$name.singular")
            val abbrKey = "rebar.unit.$name.abbr"
            val abbr = Component.translatable(abbrKey).takeIf {
                Rebar.translator.canTranslate(abbrKey, Rebar.defaultLanguage)
            }
            return UnitFormat(
                singular = singular,
                plural = Component.translatable("rebar.unit.$name.plural"),
                abbreviation = abbr,
                defaultPrefix = prefix,
                defaultStyle = Style.style(color),
            ).allowUseInUnitTag(name)
        }

        @JvmField
        val BLOCKS = rebar(
            "blocks",
            TextColor.color(0x1eaa56)
        )

        @JvmField
        val BLOCKS_PER_SECOND = rebar(
            "blocks_per_second",
            TextColor.color(0x0ae256),
            prefix = MetricPrefix.NONE
        )

        @JvmField
        val CHUNKS = rebar(
            "chunks",
            TextColor.color(0x136D37)
        )

        @JvmField
        val HEARTS = rebar(
            "hearts",
            TextColor.color(0xdb3b43)
        )

        @JvmField
        val PERCENT = rebar(
            "percent",
            TextColor.color(0xa6dd58)
        )

        @JvmField
        val RESEARCH_POINTS = rebar(
            "research_points",
            TextColor.color(0x70da65)
        )

        @JvmField
        val CELSIUS = rebar(
            "celsius",
            TextColor.color(0xe27f41)
        )

        @JvmField
        val MILLIBUCKETS = rebar(
            "buckets",
            TextColor.color(0xe3835f2),
            prefix = MetricPrefix.MILLI
        )

        @JvmField
        val MILLIBUCKETS_PER_SECOND = rebar(
            "buckets_per_second",
            TextColor.color(0xe3835f2),
            prefix = MetricPrefix.MILLI
        )

        @JvmField
        val MILLIBUCKETS_PER_ITEM = rebar(
            "buckets_per_item",
            TextColor.color(0xe3835f2),
            prefix = MetricPrefix.MILLI
        )

        @JvmField
        val DAYS = rebar(
            "days",
            TextColor.color(0xc9c786)
        )

        @JvmField
        val HOURS = rebar(
            "hours",
            TextColor.color(0xc9c786)
        )

        @JvmField
        val MINUTES = rebar(
            "minutes",
            TextColor.color(0xc9c786)
        )

        @JvmField
        val SECONDS = rebar(
            "seconds",
            TextColor.color(0xc9c786),
        )

        @JvmField
        val JOULES = rebar(
            "joules",
            TextColor.color(0xF2A900),
            prefix = MetricPrefix.NONE
        )

        @JvmField
        val WATTS = rebar(
            "watts",
            TextColor.color(0xF2A900),
            prefix = MetricPrefix.NONE
        )

        @JvmField
        val EXPERIENCE = rebar(
            "experience",
            TextColor.color(0xb2e01a)
        )

        @JvmField
        val EXPERIENCE_PER_SECOND = rebar(
            "experience_per_second",
            TextColor.color(0xb2e01a)
        )

        @JvmField
        val ITEMS = rebar(
            "items",
            TextColor.color(0x09e2c2)
        )

        @JvmField
        val ITEMS_PER_SECOND = rebar(
            "items_per_second",
            TextColor.color(0x09e2c2),
            prefix = MetricPrefix.NONE
        )

        @JvmField
        val STACKS = rebar(
            "stacks",
            TextColor.color(0x44d2e2)
        )

        @JvmField
        val CYCLES_PER_SECOND = rebar(
            "cycles_per_second",
            TextColor.color(0xb672bf),
            prefix = MetricPrefix.NONE
        )

        /**
         * Helper function that automatically formats a duration into `<days> <hours> <minutes> <seconds> <milliseconds>?`,
         * skipping any that are 0.
         *
         * @param duration the duration to format
         * @param abbreviate whether to abbreviate the units
         * @param useMillis whether to add milliseconds
         */
        @JvmStatic
        @JvmOverloads
        fun formatDuration(duration: Duration, abbreviate: Boolean = true, useMillis: Boolean = false): Component {
            var component = Component.text()
            var isEmpty = true

            val days = duration.toDaysPart()
            if (days > 0) {
                component = component.append(
                    DAYS.format(days)
                        .abbreviate(false)
                )
                isEmpty = false
            }
            val hours = duration.toHoursPart()
            if (hours > 0) {
                if (!isEmpty) {
                    component = component.append(Component.text(" "))
                }
                component = component.append(
                    HOURS.format(hours)
                        .abbreviate(abbreviate)
                )
                isEmpty = false
            }
            val minutes = duration.toMinutesPart()
            if (minutes > 0) {
                if (!isEmpty) {
                    component = component.append(Component.text(" "))
                }
                component = component.append(
                    MINUTES.format(minutes)
                        .abbreviate(abbreviate)
                )
                isEmpty = false
            }
            val seconds = duration.toSecondsPart()
            if (seconds > 0 || (!useMillis && isEmpty)) {
                if (!isEmpty) {
                    component = component.append(Component.text(" "))
                }
                component = component.append(
                    SECONDS.format(seconds)
                        .abbreviate(abbreviate)
                )
                isEmpty = false
            }
            if (useMillis) {
                val millis = duration.toMillisPart()
                if (millis > 0 || isEmpty) {
                    if (!isEmpty) {
                        component = component.append(Component.text(" "))
                    }
                    component = component.append(
                        SECONDS.format(millis / 1000.0)
                            .prefix(MetricPrefix.MILLI)
                            .abbreviate(abbreviate)
                    )
                    isEmpty = false
                }
            }
            return component.build()
        }
    }
}
