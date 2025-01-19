package io.github.pylonmc.pylon.core.persistence

import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataType

interface PylonDataWriter {
    fun <P : Any?, C : Any?> set(key: NamespacedKey, type: PersistentDataType<P, C>, value: C & Any)
}
