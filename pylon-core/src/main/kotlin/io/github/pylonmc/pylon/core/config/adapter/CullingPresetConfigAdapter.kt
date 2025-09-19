package io.github.pylonmc.pylon.core.config.adapter

import io.github.pylonmc.pylon.core.block.textures.CullingPreset
import org.bukkit.configuration.ConfigurationSection
import java.lang.reflect.Type

object CullingPresetConfigAdapter : ConfigAdapter<CullingPreset> {
    override val type: Type = CullingPreset::class.java

    override fun convert(value: Any): CullingPreset {
        if (value is ConfigurationSection) {
            return CullingPreset(
                id = value.getString("id") ?: throw IllegalArgumentException("CullingPreset is missing 'id'"),
                material = ConfigAdapter.MATERIAL.convert(value.getString("material")
                    ?: throw IllegalArgumentException("CullingPreset is missing 'material'")),
                hiddenInterval = value.getInt("hidden-interval", 1),
                visibleInterval = value.getInt("visible-interval", 20),
                alwaysShowRadius = value.getInt("always-show-radius", 16),
                cullRadius = value.getInt("cull-radius", 64),
                maxOccludingCount = value.getInt("max-occluding-count", 3)
            )
        }
        throw IllegalArgumentException("Cannot convert $value to CullingPreset")
    }
}