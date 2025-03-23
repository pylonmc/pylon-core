package io.github.pylonmc.pylon.core.item

import io.github.pylonmc.pylon.core.util.fromMiniMessage
import net.kyori.adventure.text.Component

enum class Quantity(val component: Component) {
    BLOCKS(fromMiniMessage("<#1eaa56>blocks</#1eaa56>")),
    SECONDS(fromMiniMessage("<#dbd53b>seconds</#dbd53b>")),
    HEARTS(fromMiniMessage("<#db3b43>hearts</#db3b43>")),
    NONE(fromMiniMessage(""))
}