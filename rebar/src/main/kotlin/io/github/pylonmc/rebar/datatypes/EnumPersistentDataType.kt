package io.github.pylonmc.rebar.datatypes

import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType

/**
 * A [PersistentDataType] that can be used with any enum class (such as [org.bukkit.Material]).
 *
 * Assumes that the enum names do not change between serialization and deserialization. (For
 * example, if you serialize `Sound.EPIC_SOUND` and later rename it to `Sound.GREAT_SOUND`, this
 * will obviously fail to deserialize as `Sound.EPIC_SOUND` no longer exists).
 */
class EnumPersistentDataType<E : Enum<E>>(val enumClass: Class<E>) : PersistentDataType<String, E> {

    private val valueMap: Map<String, E> = enumClass.enumConstants.associateBy { it.name }

    override fun getPrimitiveType(): Class<String> = String::class.java

    override fun getComplexType(): Class<E> = enumClass

    override fun toPrimitive(complex: E, context: PersistentDataAdapterContext): String {
        return complex.name
    }

    override fun fromPrimitive(primitive: String, context: PersistentDataAdapterContext): E {
        return valueMap[primitive] ?: throw IllegalArgumentException("Unknown enum value: $primitive")
    }

    companion object {
        fun <E : Enum<E>> enumTypeFrom(enumClass: Class<E>): EnumPersistentDataType<E> {
            return EnumPersistentDataType(enumClass)
        }

        inline fun <reified E : Enum<E>> enumTypeFrom(): EnumPersistentDataType<E> {
            return EnumPersistentDataType(E::class.java)
        }
    }
}