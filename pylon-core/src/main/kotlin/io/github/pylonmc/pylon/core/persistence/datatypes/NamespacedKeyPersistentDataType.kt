package io.github.pylonmc.pylon.core.persistence.datatypes

import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType

object NamespacedKeyPersistentDataType : PersistentDataType<String, NamespacedKey> {
    override fun getPrimitiveType(): Class<String> = String::class.java

    override fun getComplexType(): Class<NamespacedKey> = NamespacedKey::class.java

    override fun fromPrimitive(primitive: String, context: PersistentDataAdapterContext): NamespacedKey =
        NamespacedKey.fromString(primitive)!!

    override fun toPrimitive(complex: NamespacedKey, context: PersistentDataAdapterContext): String = complex.toString()
}