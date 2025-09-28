package io.github.pylonmc.pylon.core.guide.pages

import io.github.pylonmc.pylon.core.guide.pages.base.SimpleStaticGuidePage
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.Material

/**
 * Displays buttons for info sections.
 */
class InfoPage : SimpleStaticGuidePage(
    pylonKey("info"),
    Material.LANTERN
)