package io.github.pylonmc.pylon.core.util.gui.unit

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.util.EnumSet

class UnitFormat @JvmOverloads constructor(
    val name: String,
    val singular: Component,
    val plural: Component,
    val abbreviation: Component? = null,
    val defaultPrefix: MetricPrefix = MetricPrefix.NONE,
    val defaultStyle: Style = Style.empty(),
    val usePrefixes: Boolean = true
) {

    private constructor(
        name: String,
        color: TextColor,
        abbreviate: Boolean,
        prefix: MetricPrefix? = null,
    ) : this(
        name = name,
        singular = Component.translatable("pylon.pyloncore.unit.$name.singular"),
        plural = Component.translatable("pylon.pyloncore.unit.$name.plural"),
        abbreviation = Component.translatable("pylon.pyloncore.unit.$name.abbr").takeIf { abbreviate },
        defaultPrefix = prefix ?: MetricPrefix.NONE,
        defaultStyle = Style.style(color),
        usePrefixes = prefix != null
    )

    init {
        allUnits[name] = this
    }

    fun format(value: BigDecimal) = Formatted(value.stripTrailingZeros())
    fun format(value: Int) = format(BigDecimal.valueOf(value.toLong()))
    fun format(value: Long) = format(BigDecimal.valueOf(value))
    fun format(value: Float) = format(BigDecimal.valueOf(value.toDouble()))
    fun format(value: Double) = format(BigDecimal.valueOf(value))

    inner class Formatted internal constructor(private val value: BigDecimal) : ComponentLike {
        private var sigFigs = value.precision()
        private var decimalPlaces = value.scale()
        private var forceDecimalPlaces = false
        private var abbreviate = true
        private var unitStyle = defaultStyle
        private var prefix: MetricPrefix = defaultPrefix
        private val badPrefixes = EnumSet.noneOf(MetricPrefix::class.java)

        fun significantFigures(sigFigs: Int) = apply { this.sigFigs = sigFigs }
        fun decimalPlaces(decimalPlaces: Int) = apply { this.decimalPlaces = decimalPlaces }
        fun forceDecimalPlaces(force: Boolean) = apply { this.forceDecimalPlaces = force }
        fun abbreviate(abbreviate: Boolean) = apply { this.abbreviate = abbreviate }
        fun unitStyle(style: Style) = apply { this.unitStyle = style }
        fun prefix(prefix: MetricPrefix) = apply { this.prefix = prefix }
        fun ignorePrefixes(prefixes: Collection<MetricPrefix>) = apply { badPrefixes.addAll(prefixes) }
        fun ignorePrefixes(vararg prefixes: MetricPrefix) = apply { badPrefixes.addAll(prefixes) }

        fun autoSelectPrefix() = apply {
            val absValue = value.abs()
            val exponent = -absValue.scale()
            if (exponent != 0) {
                val prefix = MetricPrefix.entries.firstOrNull { it.scale <= exponent }
                if (prefix != null) {
                    this.prefix = prefix
                }
            }
        }

        override fun asComponent(): Component {
            var usedPrefix = prefix
            while (usedPrefix in badPrefixes) {
                usedPrefix = MetricPrefix.entries[MetricPrefix.entries.indexOf(usedPrefix) + 1]
            }
            if (!usePrefixes) {
                usedPrefix = MetricPrefix.NONE
            }

            var usedValue = value.movePointRight(usedPrefix.scale - defaultPrefix.scale)
            usedValue = usedValue.round(MathContext(sigFigs, RoundingMode.HALF_UP))
            usedValue = usedValue.setScale(decimalPlaces, RoundingMode.HALF_UP)
            if (!forceDecimalPlaces && usedValue.scale() > 0) {
                usedValue = usedValue.stripTrailingZeros()
            }

            val number = Component.text(usedValue.toPlainString())
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

            return number.append(Component.text(" ")).append(unit)
        }
    }

    companion object {

        @JvmSynthetic
        internal val allUnits = mutableMapOf<String, UnitFormat>()

        @JvmField
        val BLOCKS = UnitFormat(
            "blocks",
            TextColor.color(0x1eaa56),
            abbreviate = false
        )

        @JvmField
        val CHUNKS = UnitFormat(
            "chunks",
            TextColor.color(0x136D37),
            abbreviate = false
        )

        @JvmField
        val HEARTS = UnitFormat("hearts", TextColor.color(0xdb3b43), abbreviate = true)

        @JvmField
        val PERCENT = UnitFormat(
            "percent",
            TextColor.color(0xa0cb29),
            abbreviate = true
        )

        @JvmField
        val RESEARCH_POINTS = UnitFormat(
            "research_points",
            TextColor.color(0x70da65),
            abbreviate = false
        )

        @JvmField
        val CELSIUS = UnitFormat(
            "celsius",
            TextColor.color(0xe27f41),
            abbreviate = true
        )

        @JvmField
        val MILLIBUCKETS = UnitFormat(
            "buckets",
            TextColor.color(0xe3835f2),
            abbreviate = true,
            prefix = MetricPrefix.MILLI
        )

        @JvmField
        val MILLIBUCKETS_PER_SECOND = UnitFormat(
            "buckets_per_second",
            TextColor.color(0xe3835f2),
            abbreviate = true,
            prefix = MetricPrefix.MILLI
        )

        @JvmField
        val DAYS = UnitFormat(
            "days",
            TextColor.color(0xc9c786),
            abbreviate = true
        )

        @JvmField
        val HOURS = UnitFormat(
            "hours",
            TextColor.color(0xc9c786),
            abbreviate = true
        )

        @JvmField
        val MINUTES = UnitFormat(
            "minutes",
            TextColor.color(0xc9c786),
            abbreviate = true
        )

        @JvmField
        val SECONDS = UnitFormat(
            "seconds",
            TextColor.color(0xc9c786),
            abbreviate = true
        )

        @JvmField
        val JOULES = UnitFormat(
            "joules",
            TextColor.color(0xF2A900),
            abbreviate = true,
            prefix = MetricPrefix.NONE
        )

        @JvmField
        val WATTS = UnitFormat(
            "watts",
            TextColor.color(0xF2A900),
            abbreviate = true,
            prefix = MetricPrefix.NONE
        )
    }
}