package io.github.pylonmc.pylon.core.config

import io.github.pylonmc.pylon.core.util.key.getAddon
import org.bukkit.NamespacedKey

object Settings {

    @JvmStatic
    fun get(key: NamespacedKey): Config
            = getAddon(key).mergeGlobalConfig("settings/${key.key}.yml", "settings/${key.namespace}/${key.key}")
}