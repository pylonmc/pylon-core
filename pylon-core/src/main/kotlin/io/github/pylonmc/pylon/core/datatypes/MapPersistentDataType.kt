package io.github.pylonmc.pylon.core.datatypes

class MapPersistentDataType<KP : Any, KC : Any, VP : Any, VC : Any>(
    private val keyType: PersistentDataType<KP, KC>,
    private val valueType: PersistentDataType<VP, VC>
) : PersistentDataType<PersistentDataContainer, Map<KC, VC>> {

    private val mapKeys = NamespacedKey(pluginInstance, "keys")
    private val mapValues = NamespacedKey(pluginInstance, "values")

    fun keyType(): PersistentDataType<KP, KC> = keyType

    fun valueType(): PersistentDataType<VP, VC> = valueType

    override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java

    @Suppress("UNCHECKED_CAST")
    override fun getComplexType(): Class<Map<KC, VC>> = Map::class.java as Class<Map<KC, VC>>

    override fun toPrimitive(
        complex: Map<KC, VC>,
        context: PersistentDataAdapterContext
    ): PersistentDataContainer {
        val primitive = context.newPersistentDataContainer()
        primitive.set(mapKeys, PylonSerializers.LIST.listTypeFrom(keyType), complex.keys.toList())
        primitive.set(mapValues, PylonSerializers.LIST.listTypeFrom(valueType), complex.values.toList())
        return primitive
    }

    override fun fromPrimitive(
        primitive: PersistentDataContainer,
        context: PersistentDataAdapterContext
    ): Map<KC, VC> {
        val keys = primitive.get(mapKeys, PylonSerializers.LIST.listTypeFrom(keyType))!!
        val values = primitive.get(mapValues, PylonSerializers.LIST.listTypeFrom(valueType))!!
        return keys.zip(values).toMap()
    }

    companion object {
        fun <KP : Any, KC : Any, VP : Any, VC : Any> mapTypeFrom(
            keyType: PersistentDataType<KP, KC>,
            valueType: PersistentDataType<VP, VC>
        ): MapPersistentDataType<KP, KC, VP, VC> = MapPersistentDataType(keyType, valueType)
    }
}