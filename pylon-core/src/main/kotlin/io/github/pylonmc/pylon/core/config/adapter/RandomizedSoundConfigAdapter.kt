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
            ConfigAdapter.ENUM.from<Sound.Source>().convert(map["source"]!!),
            getRange(map, "volume"),
            getRange(map, "pitch")
        )
    }

    private fun getRange(section: Map<String, Any>, key: String): Pair<Double, Double> {
        val range = section[key]!!
        if (range is ConfigurationSection || range is Map<*, *>) {
            val range = MapConfigAdapter.STRING_TO_ANY.convert(range)
            return Pair(ConfigAdapter.DOUBLE.convert(range["min"]!!), ConfigAdapter.DOUBLE.convert(range["max"]!!))
        } else if (range is List<*>) {
            return Pair(ConfigAdapter.DOUBLE.convert(range[0]!!), ConfigAdapter.DOUBLE.convert(range[1]!!))
        } else {
            val value = ConfigAdapter.DOUBLE.convert(range)
            return Pair(value, value)
        }
    }
}