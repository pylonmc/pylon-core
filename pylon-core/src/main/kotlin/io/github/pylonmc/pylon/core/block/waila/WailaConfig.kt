package io.github.pylonmc.pylon.core.block.waila

import io.github.pylonmc.pylon.core.i18n.PylonArgument
import io.github.pylonmc.pylon.core.i18n.PylonArgument.Companion.attachPylonArguments
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

/**
 * The configuration for a WAILA bar, returned by a block or entity
 */
data class WailaConfig @JvmOverloads constructor(
    val text: Component,
    val placeholders: List<PylonArgument> = emptyList(),
    val color: BossBar.Color = BossBar.Color.WHITE,
    val style: BossBar.Overlay = BossBar.Overlay.PROGRESS,
    val progress: Float = 1F
) {

    @JvmSynthetic
    internal fun apply(bar: BossBar) {
        val player = bar.viewers().singleOrNull() as? Player
        if (player != null) {
            bar.name(text.attachPylonArguments(placeholders))
        } else {
            bar.name(text)
        }
        bar.color(color)
        bar.overlay(style)
        bar.progress(progress)
    }
}