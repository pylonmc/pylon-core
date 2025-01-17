package io.github.pylonmc.pylon.core.state

import org.bukkit.NamespacedKey

interface StateReader {
    val id: NamespacedKey
}