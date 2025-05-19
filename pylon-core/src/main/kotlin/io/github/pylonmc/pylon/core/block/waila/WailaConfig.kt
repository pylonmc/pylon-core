package io.github.pylonmc.pylon.core.block.waila

import io.github.pylonmc.pylon.core.i18n.PlaceholderAttacher
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

data class WailaConfig @JvmOverloads constructor(
    val text: Component,
    val placeholders: Map<String, Component> = emptyMap(),
    val color: BossBar.Color = BossBar.Color.WHITE,
    val style: BossBar.Overlay = BossBar.Overlay.PROGRESS,
    val progress: Float = 1F
) {

    fun apply(bar: BossBar) {
        val player = bar.viewers().singleOrNull() as? Player
        if (player != null) {
            val attacher = PlaceholderAttacher(placeholders)
            bar.name(attacher.render(text, Unit))
        } else {
            bar.name(text)
        }
        bar.color(color)
        bar.overlay(style)
        bar.progress(progress)
    }
}