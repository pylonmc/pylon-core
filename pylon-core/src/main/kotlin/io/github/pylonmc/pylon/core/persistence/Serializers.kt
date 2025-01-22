package io.github.pylonmc.pylon.core.persistence

import io.github.pylonmc.pylon.core.pluginInstance
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import java.util.*

object Serializers {
    @JvmField
    val BYTE = PersistentDataType.BYTE!!
    @JvmField
    val SHORT = PersistentDataType.SHORT!!
    @JvmField
    val INTEGER = PersistentDataType.INTEGER!!
    @JvmField
    val LONG = PersistentDataType.LONG!!
    @JvmField
    val FLOAT = PersistentDataType.FLOAT!!
    @JvmField
    val DOUBLE = PersistentDataType.DOUBLE!!
    @JvmField
    val BOOLEAN = PersistentDataType.BOOLEAN!!
    @JvmField
    val STRING = PersistentDataType.STRING!!
    @JvmField
    val BYTE_ARRAY = PersistentDataType.BYTE_ARRAY!!
    @JvmField
    val INTEGER_ARRAY = PersistentDataType.INTEGER_ARRAY!!
    @JvmField
    val LONG_ARRAY = PersistentDataType.LONG_ARRAY!!
    @JvmField
    val TAG_CONTAINER = PersistentDataType.TAG_CONTAINER!!
    @JvmField
    val NAMESPACED_KEY = NamespacedKeyPersistentDataType()
    @JvmField
    val UUID = UUIDPersistentDataType()
    @JvmField
    val LIST = PersistentDataType.LIST!!
    @JvmField
    val SET = SetPersistentDataTypeProvider()
    @JvmField
    val MAP = MapPersistentDataTypeProvider()
}

class NamespacedKeyPersistentDataType : PersistentDataType<String, NamespacedKey> {
    override fun getPrimitiveType(): Class<String>
        = String::class.java

    override fun getComplexType(): Class<NamespacedKey>
        = NamespacedKey::class.java

    override fun fromPrimitive(primitive: String, context: PersistentDataAdapterContext): NamespacedKey
        = NamespacedKey.fromString(primitive)!!

    override fun toPrimitive(complex: NamespacedKey, context: PersistentDataAdapterContext): String
        = complex.toString()
}

class UUIDPersistentDataType : PersistentDataType<LongArray, UUID> {
    override fun getPrimitiveType(): Class<LongArray>
            = LongArray::class.java

    override fun getComplexType(): Class<UUID>
            = UUID::class.java

    override fun fromPrimitive(primitive: LongArray, context: PersistentDataAdapterContext): UUID {
        return UUID(primitive[0], primitive[1])
    }

    override fun toPrimitive(complex: UUID, context: PersistentDataAdapterContext): LongArray {
        return longArrayOf(complex.mostSignificantBits, complex.leastSignificantBits)
    }
}

interface SetPersistentDataType<P, C> : PersistentDataType<PersistentDataContainer, Set<C>> {
    fun elementType(): PersistentDataType<P, C>
}

// 'Hold on, why the hell are we returning an entire PDC instead of a list of primitives???'
// Well for some reason, lists are only counted as primitives if accompanied by a ListPersistentDataType (wtf)
class SetPersistentDataTypeProvider {
    fun <P: Any, C: Any> setTypeFrom(elementType: PersistentDataType<P, C>): SetPersistentDataType<P, C>
            = SetPersistentDataTypeImpl(elementType)

    private class SetPersistentDataTypeImpl<P: Any, C: Any>(
        private val elementType: PersistentDataType<P, C>
    ) : SetPersistentDataType<P, C> {

        private val setValues = NamespacedKey(pluginInstance, "values")

        override fun elementType(): PersistentDataType<P, C>
            = elementType

        override fun getPrimitiveType(): Class<PersistentDataContainer>
            = PersistentDataContainer::class.java

        @Suppress("UNCHECKED_CAST") // Yes, this is cursed, no, there's no way around it afaik
        override fun getComplexType(): Class<Set<C>>
            = Set::class.java as Class<Set<C>>

        override fun toPrimitive(complex: Set<C>, context: PersistentDataAdapterContext): PersistentDataContainer {
            val pdc = context.newPersistentDataContainer()
            pdc.set(setValues, PersistentDataType.LIST.listTypeFrom(elementType), ArrayList(complex))
            return pdc
        }

        override fun fromPrimitive(primitive: PersistentDataContainer, context: PersistentDataAdapterContext): Set<C>
            = primitive.get(setValues, PersistentDataType.LIST.listTypeFrom(elementType))!!.toSet()
    }
}

interface MapPersistentDataType<KP, KC, VP, VC> : PersistentDataType<PersistentDataContainer, Map<KC, VC>> {
    fun keyType(): PersistentDataType<KP, KC>
    fun valueType(): PersistentDataType<VP, VC>
}

class MapPersistentDataTypeProvider {
    fun <KP: Any, KC: Any, VP: Any, VC: Any> mapTypeFrom(
        keyType: PersistentDataType<KP, KC>,
        valueType: PersistentDataType<VP, VC>
    ): MapPersistentDataType<KP, KC, VP, VC>
        = MapPersistentDataTypeImpl(keyType, valueType)

    private class MapPersistentDataTypeImpl<KP: Any, KC: Any, VP: Any, VC: Any>(
        private val keyType: PersistentDataType<KP, KC>,
        private val valueType: PersistentDataType<VP, VC>
    ) : MapPersistentDataType<KP, KC, VP, VC> {

        private val mapKeys = NamespacedKey(pluginInstance, "keys")
        private val mapValues = NamespacedKey(pluginInstance, "values")

        override fun keyType(): PersistentDataType<KP, KC>
            = keyType

        override fun valueType(): PersistentDataType<VP, VC>
            = valueType

        override fun getPrimitiveType(): Class<PersistentDataContainer>
            = PersistentDataContainer::class.java

        @Suppress("UNCHECKED_CAST")
        override fun getComplexType(): Class<Map<KC, VC>>
            = Map::class.java as Class<Map<KC, VC>>

        override fun toPrimitive(
            complex: Map<KC, VC>,
            context: PersistentDataAdapterContext
        ): PersistentDataContainer {
            val primitive = context.newPersistentDataContainer()
            primitive.set(mapKeys, Serializers.LIST.listTypeFrom(keyType), complex.keys.toList())
            primitive.set(mapValues, Serializers.LIST.listTypeFrom(valueType), complex.values.toList())
            return primitive
        }

        override fun fromPrimitive(
            primitive: PersistentDataContainer,
            context: PersistentDataAdapterContext
        ): Map<KC, VC> {
            val keys = primitive.get(mapKeys, Serializers.LIST.listTypeFrom(keyType))!!
            val values = primitive.get(mapValues, Serializers.LIST.listTypeFrom(valueType))!!
            return keys.zip(values).toMap()
        }
    }
}
