package io.github.pylonmc.rebar.config.adapter

import io.github.pylonmc.rebar.util.RandomizedSound
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import org.bukkit.configuration.ConfigurationSection
import java.lang.reflect.Type

object RandomizedSoundConfigAdapter : ConfigAdapter<RandomizedSound> {
    override val type: Type = RandomizedSound::class.java

    override fun convert(value: Any): RandomizedSound {
        val map = MapConfigAdapter.STRING_TO_ANY.convert(value)
        val keys = mutableListOf<Key>()
        if (map.containsKey("sound")) {
            keys.add(Key.key(map["sound"]!! as String))
        } else if (map.containsKey("sounds")) {
            for (element in map["sounds"] as List<*>) {
                keys.add(Key.key(element as String) )
            }
        } else {
            throw IllegalArgumentException("No 'sound' or 'sounds' field found in section: $value")
        }

        return RandomizedSound(
            keys,
            ConfigAdapter.ENUM.from<Sound.Source>().convert(map["source"] ?: throw IllegalArgumentException("Sound is missing 'source' field")),
            getRange(map, "volume"),
            getRange(map, "pitch")
        )
    }

    private fun getRange(section: Map<String, Any>, key: String): Pair<Double, Double> {
        val range = section[key] ?: throw IllegalArgumentException("Sound is missing '$key' field")
        if (range is ConfigurationSection || range is Map<*, *>) {
            val range = MapConfigAdapter.STRING_TO_ANY.convert(range)
            return Pair(
                ConfigAdapter.DOUBLE.convert(range["min"] ?: throw IllegalArgumentException("Sound is missing '$key.min' field")),
                ConfigAdapter.DOUBLE.convert(range["max"] ?: throw IllegalArgumentException("Sound is missing '$key.max' field"))
            )
        } else if (range is List<*>) {
            return Pair(ConfigAdapter.DOUBLE.convert(range[0]!!), ConfigAdapter.DOUBLE.convert(range[1]!!))
        } else {
            try {
                val value = range.toString().toDouble()
                return Pair(value, value)
            } catch (_: Throwable) {
                throw IllegalArgumentException("Sound '$key' field is not a valid number or range: $range")
            }
        }
    }
}