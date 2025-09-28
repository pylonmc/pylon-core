package io.github.pylonmc.pylon.core.config.adapter

import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound 
import java.lang.reflect.Type

object SoundConfigAdapter : ConfigAdapter<Sound> {
    override val type: Type = Sound::class.java

    override fun convert(value: Any): Sound {
        val map = MapConfigAdapter.STRING_TO_ANY.convert(value)
        return Sound.sound(
            Key.key(map["name"] as String),
            ConfigAdapter.ENUM.from<Sound.Source>().convert(map["source"] ?: throw IllegalArgumentException("Sound is missing 'source' field")),
            ConfigAdapter.FLOAT.convert(map["volume"] ?: throw IllegalArgumentException("Sound is missing 'volume' field")),
            ConfigAdapter.FLOAT.convert(map["pitch"] ?: throw IllegalArgumentException("Sound is missing 'pitch' field"))
        )
    }
}