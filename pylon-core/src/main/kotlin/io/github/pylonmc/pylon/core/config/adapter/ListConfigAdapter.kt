package io.github.pylonmc.pylon.core.config.adapter

import org.apache.commons.lang3.reflect.TypeUtils
import org.bukkit.configuration.ConfigurationSection
import java.lang.reflect.Type

class ListConfigAdapter<E>(private val elementAdapter: ConfigAdapter<E>) : ConfigAdapter<List<E>> {

    override val type: Type = TypeUtils.parameterize(List::class.java, elementAdapter.type)

    override fun convert(value: Any): List<E> {
        val list = when (value) {
            is List<*> -> value
            is ConfigurationSection, is Map<*, *> -> SectionOrMap.of(value).asMap().toList()
            else -> throw IllegalArgumentException("Expected a List or ConfigurationSection, but got: ${value::class.java.name}")
        }
        return list.map {
            @Suppress("UNCHECKED_CAST")
            it?.let(elementAdapter::convert) as E
        }
    }

    companion object {
        @JvmStatic
        fun <E> from(elementAdapter: ConfigAdapter<E>): ConfigAdapter<List<E>> {
            return ListConfigAdapter(elementAdapter)
        }
    }
}