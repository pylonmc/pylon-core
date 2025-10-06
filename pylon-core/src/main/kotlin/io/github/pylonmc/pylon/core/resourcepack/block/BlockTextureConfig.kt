package io.github.pylonmc.pylon.core.resourcepack.block

import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.config.Config
import io.github.pylonmc.pylon.core.config.adapter.ConfigAdapter

object BlockTextureConfig {

    private val config = Config(PylonCore, "config.yml")

    @JvmStatic
    val blockTexturesEnabled = config.getOrThrow("custom-block-textures.enabled", ConfigAdapter.BOOLEAN)

    @JvmStatic
    val blockTexturesForced = config.getOrThrow("custom-block-textures.force", ConfigAdapter.BOOLEAN)

    @JvmStatic
    val occludingCacheRefreshInterval = config.getOrThrow("custom-block-textures.culling.occluding-cache-refresh-interval", ConfigAdapter.INT)

    @JvmStatic
    val occludingCacheRefreshShare = config.getOrThrow("custom-block-textures.culling.occluding-cache-refresh-share", ConfigAdapter.DOUBLE)

    @JvmStatic
    val cullingPresets = config.getOrThrow("custom-block-textures.culling.presets", ConfigAdapter.MAP.from(ConfigAdapter.STRING, ConfigAdapter.CULLING_PRESET))

    @JvmStatic
    val defaultCullingPreset = run {
        val key = config.getOrThrow<String>("custom-block-textures.culling.default-preset", ConfigAdapter.STRING)
        cullingPresets[key] ?: error("No culling preset with id '$key' found")
    }

}