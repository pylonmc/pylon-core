package io.github.pylonmc.pylon.core.util

import io.github.pylonmc.pylon.core.addon.PylonAddon
import io.github.pylonmc.pylon.core.pluginInstance
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.NamespacedKey

internal fun pylonKey(key: String): NamespacedKey = NamespacedKey(pluginInstance, key)

operator fun TextColor.plus(text: String): Component = Component.text(text).color(this)

fun NamespacedKey.isFromAddon(addon: PylonAddon): Boolean {
    return namespace == NamespacedKey(addon.javaPlugin, "").namespace
}
