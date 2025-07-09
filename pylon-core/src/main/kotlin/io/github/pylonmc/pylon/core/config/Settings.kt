package io.github.pylonmc.pylon.core.config

import io.github.pylonmc.pylon.core.util.key.getAddon
import org.bukkit.NamespacedKey

object Settings {

    /**
     * Retrieves the settings for the given [key]
     */
    @JvmStatic
    fun get(key: NamespacedKey): Config =
        getAddon(key).mergeGlobalConfig("settings/${key.key}.yml", "settings/${key.namespace}/${key.key}.yml")
}