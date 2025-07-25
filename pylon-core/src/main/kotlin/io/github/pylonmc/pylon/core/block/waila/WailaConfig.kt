package io.github.pylonmc.pylon.core.block.waila

import io.github.pylonmc.pylon.core.i18n.PlaceholderAttacher
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import org.bukkit.entity.Player

/**
 * The configuration for a WAILA bar, returned by a block or entity
 */
data class WailaConfig @JvmOverloads constructor(
    val text: Component,
    val placeholders: Map<String, ComponentLike> = emptyMap(),
    val color: BossBar.Color = BossBar.Color.WHITE,
    val style: BossBar.Overlay = BossBar.Overlay.PROGRESS,
    val progress: Float = 1F
) {

    @JvmSynthetic
    internal fun apply(bar: BossBar) {
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