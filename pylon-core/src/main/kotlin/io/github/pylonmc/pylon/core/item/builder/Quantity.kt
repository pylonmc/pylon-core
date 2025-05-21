package io.github.pylonmc.pylon.core.item.builder

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor

object Quantity {

    private val quantities = mutableMapOf<String, Component>()

    @JvmField
    val BLOCKS = create("blocks", "pylon.pyloncore.quantity.blocks", TextColor.color(0x1eaa56))

    @JvmField
    val SECONDS = create("seconds", "pylon.pyloncore.quantity.seconds", TextColor.color(0xc9c786))

    @JvmField
    val HEARTS = create("hearts", "pylon.pyloncore.quantity.hearts", TextColor.color(0xdb3b43))

    @JvmField
    val PERCENT = create("percent", "pylon.pyloncore.quantity.percent", TextColor.color(0xa0cb29))

    @JvmField
    val RESEARCH_POINTS = create("research_points", "pylon.pyloncore.quantity.research_points", TextColor.color(0x70da65))

    @JvmField
    val TEMPERATURE = create("temperature", "pylon.pyloncore.quantity.temperature", TextColor.color(0xe27f41))

    @JvmField
    val FLUID = create("fluid", "pylon.pyloncore.quantity.fluid", TextColor.color(0xe3835f2))

    @JvmField
    val FLUID_PER_SECOND = create("fluid_per_second", "pylon.pyloncore.quantity.fluid_per_second", TextColor.color(0xe3835f2))

    @JvmField
    val CHUNKS = create("chunks", "pylon.pyloncore.quantity.chunks", TextColor.color(0x136D37))

    @JvmStatic
    fun byName(name: String): Component? = quantities[name.lowercase()]

    @JvmStatic
    fun create(name: String, translationKey: String, color: TextColor): Component =
        Component.translatable(translationKey)
            .color(color)
            .also { quantities[name] = it }
}