package io.github.pylonmc.pylon.core.util

import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound

data class RandomizedSound(
    val keys: Collection<Key>,
    val source: Sound.Source,
    val volume: Pair<Double, Double>,
    val pitch: Pair<Double, Double>
) {
    fun create() : Sound = Sound.sound(
        keys.random(),
        source,
        (volume.first + Math.random() * (volume.second - volume.first)).toFloat(),
        (pitch.first + Math.random() * (pitch.second - pitch.first)).toFloat()
    )
}
