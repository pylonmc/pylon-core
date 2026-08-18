package io.github.pylonmc.rebar.datatypes

import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType
import java.util.function.Function

/**
 * A [PersistentDataType] that can be used with any class that implements [Keyed].
 */
abstract class KeyedPersistentDataType<T : Keyed>(val type: Class<T>) : PersistentDataType<String, T> {

    override fun getPrimitiveType(): Class<String> = String::class.java

    override fun getComplexType(): Class<T> = type

    override fun toPrimitive(complex: T, context: PersistentDataAdapterContext): String {
        return RebarSerializers.NAMESPACED_KEY.toPrimitive(complex.key, context)
    }

    override fun fromPrimitive(
        primitive: String,
        context: PersistentDataAdapterContext
    ): T {
        val key = RebarSerializers.NAMESPACED_KEY.fromPrimitive(primitive, context)
        return retrieve(key)
    }

    /**
     * Gets the value of the keyed type corresponding to [T].
     *
     * For example, if you are using this for [io.github.pylonmc.rebar.item.research.Research]es,
     * you would want to do `RebarRegistry.RESEARCHES.getOrThrow(key)`.
     */
    abstract fun retrieve(key: NamespacedKey): T

    companion object {
        /**
         * Kotlin-source compatibility overload.
         *
         * This method intentionally remains in the JVM ABI for already compiled Kotlin addons, but
         * is hidden from Java source so Java plugins do not put kotlin.jvm.functions.Function1 in
         * their cross-plugin call signature.
         */
        @JvmStatic
        @JvmSynthetic
        fun <T : Keyed> keyedTypeFrom(
            type: Class<T>,
            retrievalFunction: (NamespacedKey) -> T
        ): PersistentDataType<String, T> {
            return object : KeyedPersistentDataType<T>(type) {
                override fun retrieve(key: NamespacedKey): T = retrievalFunction(key)
            }
        }

        /**
         * Java-safe overload. java.util.function.Function is loaded by the JVM/platform classloader,
         * so it is safe to use in an API call crossing the Rebar/Pylon plugin-classloader boundary.
         */
        @JvmStatic
        fun <T : Keyed> keyedTypeFrom(
            type: Class<T>,
            retrievalFunction: Function<NamespacedKey, T>
        ): PersistentDataType<String, T> {
            return object : KeyedPersistentDataType<T>(type) {
                override fun retrieve(key: NamespacedKey): T = retrievalFunction.apply(key)
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