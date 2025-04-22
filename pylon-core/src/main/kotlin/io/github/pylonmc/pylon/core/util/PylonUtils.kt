@file:JvmName("PylonUtils")

package io.github.pylonmc.pylon.core.util

import io.github.pylonmc.pylon.core.addon.PylonAddon
import org.bukkit.NamespacedKey

/*
This file is for public general utils that Java can make use of. See also `InternalUtils.kt`.
 */

fun NamespacedKey.isFromAddon(addon: PylonAddon): Boolean {
    return namespace == addon.key.namespace
}

fun Double.withDecimals(decimals: Int) = "%.${decimals}f".format(this)