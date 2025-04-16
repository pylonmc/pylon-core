package io.github.pylonmc.pylon.core.util

import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

fun <P, C> setNullable(pdc: PersistentDataContainer, key: NamespacedKey, type: PersistentDataType<P, C>, value: C?) {
    if (value != null) {
        pdc.set(key, type, value)
    } else {
        pdc.remove(key)
    }
}
