@file:JvmName("PylonUtils")

package io.github.pylonmc.pylon.core.util

import io.github.pylonmc.pylon.core.addon.PylonAddon
import org.bukkit.NamespacedKey
import java.math.BigDecimal

/*
This file is for public general utils that Java can make use of. See also `InternalUtils.kt`.
 */

fun NamespacedKey.isFromAddon(addon: PylonAddon): Boolean {
    return namespace == addon.key.namespace
}

fun Double.toCleanString(): String = BigDecimal(this).stripTrailingZeros().toPlainString()