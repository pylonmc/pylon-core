package io.github.pylonmc.pylon.core.config.adapter

import io.github.pylonmc.pylon.core.waila.WailaDisplay
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component

object WailaDisplayConfigAdapter : ConfigAdapter<WailaDisplay> {
    override val type = WailaDisplay::class.java

    override fun convert(value: Any): WailaDisplay {
        val map = MapConfigAdapter.STRING_TO_ANY.convert(value)
        val text = Component.translatable(ConfigAdapter.STRING.convert(map["text"] ?: throw IllegalArgumentException("WailaDisplay is missing 'text' field")))
        val color = ConfigAdapter.ENUM.from<BossBar.Color>().convert(map["color"] ?: throw IllegalArgumentException("WailaDisplay is missing 'color' field"))
        val overlay = ConfigAdapter.ENUM.from<BossBar.Overlay>().convert(map["overlay"] ?: throw IllegalArgumentException("WailaDisplay is missing 'overlay' field"))
        val progress = ConfigAdapter.FLOAT.convert(map["progress"] ?: throw IllegalArgumentException("WailaDisplay is missing 'progress' field"))
        return WailaDisplay(text, color, overlay, progress)
    }
}