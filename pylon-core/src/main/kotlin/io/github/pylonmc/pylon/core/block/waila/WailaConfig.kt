package io.github.pylonmc.pylon.core.block.waila

import io.github.pylonmc.pylon.core.i18n.PlaceholderAttacher
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.translation.GlobalTranslator
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar

/**
 * Configures the WAILA bossbar
 *
 * @property text The text to be displayed in the bossbar
 * @property placeholders Placeholders to be used in [text]. Defaults to empty
 * @property color The color of the bossbar. Defaults to [BarColor.WHITE]
 * @property style The style of the bossbar. Defaults to [BarStyle.SOLID]
 * @property progress The progress of the bossbar. Defaults to 1.0
 */
data class WailaConfig @JvmOverloads constructor(
    val text: Component,
    val placeholders: Map<String, Component> = emptyMap(),
    val color: BarColor = BarColor.WHITE,
    val style: BarStyle = BarStyle.SOLID,
    val progress: Double = 1.0
) {

    @JvmSynthetic
    internal fun apply(bar: BossBar) {
        val player = bar.players.single()
        val attacher = PlaceholderAttacher(placeholders)
        val translated = GlobalTranslator.render(attacher.render(text, Unit), player.locale())
        bar.setTitle(LegacyComponentSerializer.legacySection().serialize(translated))
        bar.color = color
        bar.style = style
        bar.progress = progress
    }
}