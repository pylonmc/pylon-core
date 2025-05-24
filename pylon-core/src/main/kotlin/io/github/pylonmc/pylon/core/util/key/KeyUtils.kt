@file:JvmName("KeyUtils")

package io.github.pylonmc.pylon.core.util.key

import io.github.pylonmc.pylon.core.addon.PylonAddon
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import org.bukkit.NamespacedKey

fun getAddon(key: NamespacedKey): PylonAddon
        = PylonRegistry.ADDONS.find { addon -> addon.key.namespace == key.namespace }
    ?: error("Key does not have a corresponding addon; does your addon call registerWithPylon()?")
