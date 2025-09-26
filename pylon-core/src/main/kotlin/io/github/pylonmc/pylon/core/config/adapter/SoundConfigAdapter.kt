package io.github.pylonmc.pylon.core.config.adapter

import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound 
import java.lang.reflect.Type

object SoundConfigAdapter : ConfigAdapter<Sound> {
    override val type: Type = Sound::class.java

    override fun convert(value: Any): Sound {
        val map = MapConfigAdapter.STRING_TO_ANY.convert(value)
        Sound.sound(
            Key.key(map["name"] as String),
            ConfigAdapter.ENUM.from(Sound.Source::class.java).convert(map["source"]!!),
            (map["volume"] as Number).toFloat(),
            (map["pitch"] as Number).toFloat()
        )
        throw IllegalArgumentException("Cannot convert value to Sound: $value")
    }
}