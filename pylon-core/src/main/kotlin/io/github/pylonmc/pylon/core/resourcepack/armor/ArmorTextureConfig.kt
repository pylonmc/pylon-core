package io.github.pylonmc.pylon.core.resourcepack.armor

import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.config.Config
import io.github.pylonmc.pylon.core.config.adapter.ConfigAdapter

object ArmorTextureConfig {

    private val config = Config(PylonCore, "config.yml")

    @JvmStatic
    val armorTexturesEnabled = config.getOrThrow("custom-armor-textures.enabled", ConfigAdapter.BOOLEAN)

    @JvmStatic
    val armorTexturesForced = config.getOrThrow("custom-armor-textures.force", ConfigAdapter.BOOLEAN)

}