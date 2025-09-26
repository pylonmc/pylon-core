package io.github.pylonmc.pylon.core.config.adapter

import io.github.pylonmc.pylon.core.util.RandomizedSound
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
            throw IllegalArgumentException("No sound or sounds field found in section: $value")
        }

        return RandomizedSound(
            keys,
            ConfigAdapter.ENUM.from(Sound.Source::class.java).convert(map["source"]!!),
            getRange(map, "volume"),
            getRange(map, "pitch")
        )
    }

    private fun getRange(section: Map<String, Any>, key: String): Pair<Double, Double> {
        val range = section[key]!!
        if (range is ConfigurationSection || range is Map<*, *>) {
            val range = MapConfigAdapter.STRING_TO_ANY.convert(range)
            return Pair((range["min"]!! as Number).toDouble(), (range["max"]!! as Number).toDouble())
        } else if (range is List<*>) {
            return Pair((range[0] as Number).toDouble(), (range[1] as Number).toDouble())
        } else if (range is Number) {
            val value = range.toDouble()
            return Pair(value, value)
        }
        throw IllegalArgumentException("Cannot convert value to range: $range")
    }
}