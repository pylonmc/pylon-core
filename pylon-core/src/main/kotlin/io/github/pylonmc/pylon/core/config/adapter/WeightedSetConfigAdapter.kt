package io.github.pylonmc.pylon.core.config.adapter

import io.github.pylonmc.pylon.core.util.WeightedSet
import org.apache.commons.lang3.reflect.TypeUtils
import java.lang.reflect.Type

class WeightedSetConfigAdapter<E>(private val elementAdapter: ConfigAdapter<E>) : ConfigAdapter<WeightedSet<E>> {

    override val type: Type = TypeUtils.parameterize(WeightedSet::class.java, elementAdapter.type)

    override fun convert(value: Any): WeightedSet<E> {
        return if (value is List<*>) {
            value.mapTo(WeightedSet()) {
                val map = MapConfigAdapter.STRING_TO_ANY.convert(it!!)
                val element = elementAdapter.convert(map["value"] ?: throw IllegalArgumentException("Missing 'value' key in weighted set element"))
                val weight = ConfigAdapter.FLOAT.convert(map["weight"] ?: 1f)
                WeightedSet.Element(element, weight)
            }
        } else {
            WeightedSet(elementAdapter.convert(value) to 1f)
        }
    }

    companion object {
        @JvmStatic
        fun <E> from(elementAdapter: ConfigAdapter<E>): WeightedSetConfigAdapter<E> {
            return WeightedSetConfigAdapter(elementAdapter)
        }
    }
}