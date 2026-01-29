package io.github.pylonmc.rebar.waila

import io.github.pylonmc.rebar.config.RebarConfig
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component

/**
 * The configuration for a WAILA bossbar (the bar shown at the top of your
 * screen when looking at a block).
 */
@JvmRecord
data class WailaDisplay @JvmOverloads constructor(
    val text: Component,
    val color: BossBar.Color = RebarConfig.WailaConfig.DEFAULT_DISPLAY.color,
    val overlay: BossBar.Overlay = RebarConfig.WailaConfig.DEFAULT_DISPLAY.overlay,
    val progress: Float = RebarConfig.WailaConfig.DEFAULT_DISPLAY.progress
)