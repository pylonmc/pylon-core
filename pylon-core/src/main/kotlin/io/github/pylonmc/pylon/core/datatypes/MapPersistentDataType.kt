package io.github.pylonmc.pylon.core.datatypes

import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

class MapPersistentDataType<K, V>(
    keyType: PersistentDataType<*, K>,
    valueType: PersistentDataType<*, V>
) : PersistentDataType<PersistentDataContainer, Map<K, V>> {

    private val keyListType = PersistentDataType.LIST.listTypeFrom(keyType)
    private val valueListType = PersistentDataType.LIST.listTypeFrom(valueType)

    override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java

    @Suppress("UNCHECKED_CAST")
    override fun getComplexType(): Class<Map<K, V>> = Map::class.java as Class<Map<K, V>>

    override fun toPrimitive(
        complex: Map<K, V>,
        context: PersistentDataAdapterContext
    ): PersistentDataContainer {
        val primitive = context.newPersistentDataContainer()
        val (keys, values) = complex.toList().unzip()
        primitive.set(mapKeys, keyListType, keys)
        primitive.set(mapValues, valueListType, values)
        return primitive
    }

    override fun fromPrimitive(
        primitive: PersistentDataContainer,
        context: PersistentDataAdapterContext
    ): Map<K, V> {
        val keys = primitive.get(mapKeys, keyListType)!!
        val values = primitive.get(mapValues, valueListType)!!
        return keys.zip(values).toMap().toMutableMap()
    }

    companion object {
        private val mapKeys = pylonKey("keys")
        private val mapValues = pylonKey("values")

        fun <K, V> mapTypeFrom(
            keyType: PersistentDataType<*, K>,
            valueType: PersistentDataType<*, V>
        ): PersistentDataType<PersistentDataContainer, Map<K, V>> = MapPersistentDataType(keyType, valueType)
    }
}