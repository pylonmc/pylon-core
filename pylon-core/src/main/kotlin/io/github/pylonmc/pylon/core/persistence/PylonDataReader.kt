package io.github.pylonmc.pylon.core.persistence

import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataType

interface PylonDataReader {
    /**
      * ID of the holder of this reader - for example the ID of a block
      */
    val id: NamespacedKey

    fun <P : Any, C : Any> has(key: NamespacedKey, type: PersistentDataType<P, C>): Boolean

    fun has(key: NamespacedKey): Boolean

    fun <P : Any, C : Any> get(key: NamespacedKey, type: PersistentDataType<P, C>): C?

    fun <P : Any, C : Any> getOrDefault(
        key: NamespacedKey,
        type: PersistentDataType<P, C>,
        defaultValue: C
    ): C

    fun getKeys(): Set<NamespacedKey>

    fun isEmpty(): Boolean
}