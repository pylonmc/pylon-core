package io.github.pylonmc.pylon.core.waila

import io.github.pylonmc.pylon.core.config.PylonConfig
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component

/**
 * The configuration for a WAILA bossbar (the bar shown at the top of your
 * screen when looking at a block).
 */
@JvmRecord
data class WailaDisplay @JvmOverloads constructor(
    val text: Component,
    val color: BossBar.Color = PylonConfig.WailaConfig.defaultDisplay.color,
    val overlay: BossBar.Overlay = PylonConfig.WailaConfig.defaultDisplay.overlay,
    val progress: Float = PylonConfig.WailaConfig.defaultDisplay.progress
)