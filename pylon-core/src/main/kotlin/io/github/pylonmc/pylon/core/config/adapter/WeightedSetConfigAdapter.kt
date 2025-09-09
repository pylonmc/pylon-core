package io.github.pylonmc.pylon.core.config.adapter

import io.github.pylonmc.pylon.core.util.WeightedSet
import org.apache.commons.lang3.reflect.TypeUtils
import java.lang.reflect.Type

class WeightedSetConfigAdapter<E>(private val elementAdapter: ConfigAdapter<E>) : ConfigAdapter<WeightedSet<E>> {

    override val type: Type = TypeUtils.parameterize(WeightedSet::class.java, elementAdapter.type)

    override fun convert(value: Any): WeightedSet<E> {
        return if (value is List<*>) {
            value.mapTo(WeightedSet()) {
                val section = SectionOrMap.of(it)
                val element = section.getOrThrow("value", elementAdapter)
                val weight = section.getOrThrow("weight", ConfigAdapter.FLOAT)
                WeightedSet.WeightedElement(element, weight)
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