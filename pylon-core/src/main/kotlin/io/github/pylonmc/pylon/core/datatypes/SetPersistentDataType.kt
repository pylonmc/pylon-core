package io.github.pylonmc.pylon.core.datatypes

import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

// 'Hold on, why the hell are we returning an entire PDC instead of a list of primitives???'
// Well for some reason, lists are only counted as primitives if accompanied by a ListPersistentDataType (wtf)
class SetPersistentDataType<P, C>(
    val elementType: PersistentDataType<P, C>
) : PersistentDataType<PersistentDataContainer, Set<C>> {

    private val setValues = pylonKey("values")

    override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java

    @Suppress("UNCHECKED_CAST") // Yes, this is cursed; no, there's no way around it afaik
    override fun getComplexType(): Class<Set<C>> = Set::class.java as Class<Set<C>>

    override fun toPrimitive(complex: Set<C>, context: PersistentDataAdapterContext): PersistentDataContainer {
        val pdc = context.newPersistentDataContainer()
        pdc.set(setValues, PersistentDataType.LIST.listTypeFrom(elementType), ArrayList(complex))
        return pdc
    }

    override fun fromPrimitive(primitive: PersistentDataContainer, context: PersistentDataAdapterContext): Set<C> =
        primitive.get(setValues, PersistentDataType.LIST.listTypeFrom(elementType))!!.toMutableSet()

    companion object {
        fun <P, C> setTypeFrom(elementType: PersistentDataType<P, C>): PersistentDataType<PersistentDataContainer, Set<C>> =
            SetPersistentDataType(elementType)
    }
}
