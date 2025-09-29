package io.github.pylonmc.pylon.core.config.adapter

import io.github.pylonmc.pylon.core.resourcepack.block.CullingPreset
import java.lang.reflect.Type

object CullingPresetConfigAdapter : ConfigAdapter<CullingPreset> {
    override val type: Type = CullingPreset::class.java

    override fun convert(value: Any): CullingPreset {
        val map = MapConfigAdapter.STRING_TO_ANY.convert(value)
        return CullingPreset(
            index = ConfigAdapter.INT.convert(map["index"] ?: throw IllegalArgumentException("Culling preset is missing 'index' field")),
            id = ConfigAdapter.STRING.convert(map["id"] ?: throw IllegalArgumentException("Culling preset is missing 'id' field")),
            material = ConfigAdapter.MATERIAL.convert(map["material"] ?: throw IllegalArgumentException("Culling preset is missing 'material' field")),
            updateInterval = ConfigAdapter.INT.convert(map["update-interval"] ?: throw IllegalArgumentException("Culling preset is missing 'update-interval' field")),
            hiddenInterval = map["hidden-interval"]?.let { ConfigAdapter.INT.convert(it) } ?: 1,
            visibleInterval = map["visible-interval"]?.let { ConfigAdapter.INT.convert(it) } ?: 20,
            alwaysShowRadius = map["always-show-radius"]?.let { ConfigAdapter.INT.convert(it) } ?: 16,
            cullRadius = map["cull-radius"]?.let { ConfigAdapter.INT.convert(it) } ?: 64,
            maxOccludingCount = map["max-occluding-count"]?.let { ConfigAdapter.INT.convert(it) } ?: 3
        )
    }
}