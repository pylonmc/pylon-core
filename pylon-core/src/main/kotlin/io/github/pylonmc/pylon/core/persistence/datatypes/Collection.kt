package io.github.pylonmc.pylon.core.persistence.datatypes

import io.github.pylonmc.pylon.core.pluginInstance
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

// 'Hold on, why the hell are we returning an entire PDC instead of a list of primitives???'
// Well for some reason, lists are only counted as primitives if accompanied by a ListPersistentDataType (wtf)
class SetPersistentDataType<P : Any, C : Any>(
    val elementType: PersistentDataType<P, C>
) : PersistentDataType<PersistentDataContainer, Set<C>> {

    private val setValues = NamespacedKey(pluginInstance, "values")

    override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java

    @Suppress("UNCHECKED_CAST") // Yes, this is cursed; no, there's no way around it afaik
    override fun getComplexType(): Class<Set<C>> = Set::class.java as Class<Set<C>>

    override fun toPrimitive(complex: Set<C>, context: PersistentDataAdapterContext): PersistentDataContainer {
        val pdc = context.newPersistentDataContainer()
        pdc.set(setValues, PersistentDataType.LIST.listTypeFrom(elementType), ArrayList(complex))
        return pdc
    }

    override fun fromPrimitive(primitive: PersistentDataContainer, context: PersistentDataAdapterContext): Set<C> =
        primitive.get(setValues, PersistentDataType.LIST.listTypeFrom(elementType))!!.toSet()

    companion object {
        fun <P : Any, C : Any> setTypeFrom(elementType: PersistentDataType<P, C>): SetPersistentDataType<P, C> =
            SetPersistentDataType(elementType)
    }
}

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