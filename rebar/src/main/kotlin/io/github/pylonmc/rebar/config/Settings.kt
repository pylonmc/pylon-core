package io.github.pylonmc.rebar.config

import io.github.pylonmc.rebar.util.getAddon
import io.github.pylonmc.rebar.util.mergeGlobalConfig
import org.bukkit.NamespacedKey

/**
 * Utility class.
 */
object Settings {

    /**
     * Retrieves the settings for the given [key]
     */
    @JvmStatic
    fun get(key: NamespacedKey): Config =
        mergeGlobalConfig(getAddon(key), "settings/${key.key}.yml", "settings/${key.namespace}/${key.key}.yml")
}