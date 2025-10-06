package io.github.pylonmc.pylon.core.config.adapter

import io.github.pylonmc.pylon.core.fluid.tags.FluidTemperature
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.RandomizedSound
import net.kyori.adventure.sound.Sound
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import java.lang.reflect.Type

interface ConfigAdapter<T> {

    val type: Type

    /**
     * Converts the given value obtained from config to the target type [T].
     */
    fun convert(value: Any): T

    companion object {
        // @formatter:off
        @JvmField val BYTE = ConfigAdapter { if (it is String) it.toByte() else (it as Number).toByte() }
        @JvmField val SHORT = ConfigAdapter { if (it is String) it.toShort() else (it as Number).toShort() }
        @JvmField val INT = ConfigAdapter { if (it is String) it.toInt() else (it as Number).toInt() }
        @JvmField val LONG = ConfigAdapter { if (it is String) it.toLong() else (it as Number).toLong() }
        @JvmField val FLOAT = ConfigAdapter { if (it is String) it.toFloat() else (it as Number).toFloat() }
        @JvmField val DOUBLE = ConfigAdapter { if (it is String) it.toDouble() else (it as Number).toDouble() }
        @JvmField val CHAR = ConfigAdapter { (it as String).single() }
        @JvmField val BOOLEAN = ConfigAdapter { it as Boolean }
        @JvmField val ANY = ConfigAdapter { it }

        @JvmField val STRING = ConfigAdapter { it.toString() }
        @JvmField val LIST = ListConfigAdapter
        @JvmField val SET = SetConfigAdapter
        @JvmField val MAP = MapConfigAdapter
        @JvmField val ENUM = EnumConfigAdapter

        @JvmField val KEYED = KeyedConfigAdapter
        @JvmField val NAMESPACED_KEY = ConfigAdapter { NamespacedKey.fromString(STRING.convert(it))!! }
        @JvmField val MATERIAL = KEYED.fromRegistry(Registry.MATERIAL)
        @JvmField val ITEM_STACK = ItemStackConfigAdapter
        @JvmField val BLOCK_DATA = ConfigAdapter { Bukkit.createBlockData(STRING.convert(it)) }

        /**
         * A [ConfigAdapter] for in game [Sound]s,
         * comprised of a key, source, volume and pitch.
         *
         * For example:
         * ```yaml
         * hammer-sound:
         *   sound: minecraft:block.anvil.use
         *   source: player
         *   volume: 0.5
         *   pitch: 1.0
         * ```
         */
        @JvmField val SOUND = SoundConfigAdapter

        /**
         * A [ConfigAdapter] for [RandomizedSound]s,
         * which accept either a single sound or a list of sounds, a source,
         * and ranges for volume and pitch.
         *
         * Picking a random sound and a random volume and pitch from the ranges
         * when played.
         *
         * The volume and pitch can either be specified as a single value,
         * a list of two values (min and max), or specific min and max keys.
         *
         * For example:
         * ```yaml
         * hammer-sound:
         *   sounds:
         *   - minecraft:block.anvil.use
         *   - minecraft:block.anvil.land
         *   source: player
         *   volume:
         *     min: 0.3
         *     max: 0.7
         *   pitch:
         *     - 0.8
         *     - 1.2
         *```
         */
        @JvmField val RANDOMIZED_SOUND = RandomizedSoundConfigAdapter

        @JvmField val PYLON_FLUID = KEYED.fromRegistry(PylonRegistry.FLUIDS)
        @JvmField val FLUID_TEMPERATURE = ENUM.from<FluidTemperature>()
        @JvmField val FLUID_OR_ITEM = FluidOrItemConfigAdapter
        @JvmField val RECIPE_INPUT = RecipeInputConfigAdapter
        @JvmField val RECIPE_INPUT_ITEM = RecipeInputItemAdapter
        @JvmField val RECIPE_INPUT_FLUID = RecipeInputFluidAdapter
        @JvmField val ITEM_TAG = ItemTagConfigAdapter
        @JvmField val WEIGHTED_SET = WeightedSetConfigAdapter
        @JvmField val CULLING_PRESET = CullingPresetConfigAdapter
        // @formatter:on
    }
}

@JvmSynthetic
inline fun <reified T> ConfigAdapter(crossinline convert: (Any) -> T): ConfigAdapter<T> =
    object : ConfigAdapter<T> {
        override val type: Type = T::class.java
        override fun convert(value: Any): T = convert(value)
    }