package io.github.pylonmc.pylon.core.datatypes

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