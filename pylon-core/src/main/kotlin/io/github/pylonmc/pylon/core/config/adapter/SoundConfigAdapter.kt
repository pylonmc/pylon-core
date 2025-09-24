package io.github.pylonmc.pylon.core.config.adapter

import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import org.bukkit.configuration.ConfigurationSection
import java.lang.reflect.Type

object SoundConfigAdapter : ConfigAdapter<Sound> {
    override val type: Type
        get() = Sound::class.java

    override fun convert(value: Any): Sound {
        if (value is ConfigurationSection) {
            Sound.sound(
                Key.key(value.getString("name")!!),
                Sound.Source.valueOf(value.getString("source")!!.uppercase()),
                value.getDouble("volume").toFloat(),
                value.getDouble("pitch").toFloat()
            )
        }
        throw IllegalArgumentException("Cannot convert value to Sound: $value")
    }
}