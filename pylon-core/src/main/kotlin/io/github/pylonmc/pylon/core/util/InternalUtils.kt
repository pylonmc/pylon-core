package io.github.pylonmc.pylon.core.util

import io.github.pylonmc.pylon.core.pluginInstance
import org.bukkit.NamespacedKey

internal fun String.pylonKey(): NamespacedKey = NamespacedKey(pluginInstance, this)