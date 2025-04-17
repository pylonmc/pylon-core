package io.github.pylonmc.pylon.core.block.waila

import net.kyori.adventure.text.Component
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar

data class WailaConfig(val text: Component, val color: BarColor, val style: BarStyle, val progress: Double) {

    fun apply(bar: BossBar) {
        bar.titleComponent = text
        bar.color = color
        bar.style = style
        bar.progress = progress
    }
}