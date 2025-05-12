@file:JvmName("PdcUtils")

package io.github.pylonmc.pylon.core.util

import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataHolder
import org.bukkit.persistence.PersistentDataType
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun <P, C> PersistentDataContainer.setNullable(key: NamespacedKey, type: PersistentDataType<P, C>, value: C?) {
    if (value != null) {
        set(key, type, value)
    } else {
        remove(key)
    }
}

/**
 * Acts as a property delegate for stuff contained inside a [PersistentDataContainer]
 * For example:
 * ```
 * val numberOfTimesJumped: Int by persistentData(NamespacedKey(yourPlugin, "jumped"), PersistentDataType.INTEGER) { 0 }
 * ```
 */
@JvmSynthetic
inline fun <T> persistentData(
    key: NamespacedKey,
    type: PersistentDataType<*, T & Any>,
    crossinline default: () -> T
) = object : ReadWriteProperty<PersistentDataHolder, T> {

    override fun getValue(thisRef: PersistentDataHolder, property: KProperty<*>): T {
        return thisRef.persistentDataContainer.get(key, type) ?: default()
    }

    override fun setValue(thisRef: PersistentDataHolder, property: KProperty<*>, value: T) {
        if (value == null) {
            thisRef.persistentDataContainer.remove(key)
        } else {
            thisRef.persistentDataContainer.set(key, type, value)
        }
    }
}

/**
 * Same as [persistentData] but with a default value that is constant
 */
@JvmSynthetic
fun <T> persistentData(
    key: NamespacedKey,
    type: PersistentDataType<*, T & Any>,
    default: T
) = persistentData(key, type) { default }
