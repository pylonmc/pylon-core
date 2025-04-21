package io.github.pylonmc.pylon.core.block.waila

import io.github.pylonmc.pylon.core.i18n.PlaceholderAttacher
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.translation.GlobalTranslator
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar

data class WailaConfig @JvmOverloads constructor(
    val text: Component,
    val placeholders: Map<String, Component> = emptyMap(),
    val color: BarColor = BarColor.WHITE,
    val style: BarStyle = BarStyle.SOLID,
    val progress: Double = 1.0
) {

    fun apply(bar: BossBar) {
        val player = bar.players.single()
        val attacher = PlaceholderAttacher(placeholders)
        val translated = GlobalTranslator.render(attacher.render(text, Unit), player.locale())
        bar.setTitle(LegacyComponentSerializer.legacySection().serialize(translated))
        bar.color = color
        bar.style = style
        bar.progress = progress
    }
}