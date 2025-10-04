package io.github.pylonmc.pylon.core.datatypes

import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType

/**
 * A [PersistentDataType] that can be used with any class that implements [Keyed].
 */
abstract class KeyedPersistentDataType<T : Keyed>(val type: Class<T>) : PersistentDataType<String, T> {

    override fun getPrimitiveType(): Class<String> = String::class.java

    override fun getComplexType(): Class<T> = type

    override fun toPrimitive(complex: T, context: PersistentDataAdapterContext): String {
        return PylonSerializers.NAMESPACED_KEY.toPrimitive(complex.key, context)
    }

    override fun fromPrimitive(
        primitive: String,
        context: PersistentDataAdapterContext
    ): T {
        val key = PylonSerializers.NAMESPACED_KEY.fromPrimitive(primitive, context)
        return retrieve(key)
    }

    /**
     * Gets the value of the keyed type corresponding to [T].
     *
     * For example, if you are using this for [io.github.pylonmc.pylon.core.item.research.Research]es,
     * you would want to do `PylonRegistry.RESEARCHES.getOrThrow(key)`.
     */
    abstract fun retrieve(key: NamespacedKey): T

    companion object {
        @JvmStatic
        fun <T : Keyed> keyedTypeFrom(
            type: Class<T>,
            retrievalFunction: (NamespacedKey) -> T
        ): PersistentDataType<String, T> {
            return object : KeyedPersistentDataType<T>(type) {
                override fun retrieve(key: NamespacedKey): T = retrievalFunction(key)
            }
        }

        @JvmSynthetic
        inline fun <reified T : Keyed> keyedTypeFrom(
            crossinline retrievalFunction: (NamespacedKey) -> T
        ): PersistentDataType<String, T> {
            return object : KeyedPersistentDataType<T>(T::class.java) {
                override fun retrieve(key: NamespacedKey): T = retrievalFunction(key)
            }
        }
    }
}