package io.github.pylonmc.pylon.core.block.waila

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component

/**
 * The configuration for a WAILA bar, returned by a block or entity
 */
data class WailaConfig @JvmOverloads constructor(
    val text: Component,
    val color: BossBar.Color = BossBar.Color.WHITE,
    val style: BossBar.Overlay = BossBar.Overlay.PROGRESS,
    val progress: Float = 1F
) {

    @JvmSynthetic
    internal fun apply(bar: BossBar) {
        bar.name(text)
        bar.color(color)
        bar.overlay(style)
        bar.progress(progress)
    }
}