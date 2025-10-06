package io.github.pylonmc.pylon.core.waila

import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.config.Config
import io.github.pylonmc.pylon.core.config.adapter.ConfigAdapter
import net.kyori.adventure.bossbar.BossBar

object WailaConfig {
    private val config = Config(PylonCore, "config.yml")

    @JvmStatic
    val wailaEnabled
        get() = tickInterval > 0 && enabledTypes.isNotEmpty()

    @JvmStatic
    val tickInterval = config.getOrThrow("waila.tick-interval", ConfigAdapter.INT)

    @JvmStatic
    val enabledTypes = config.getOrThrow("waila.enabled-types", ConfigAdapter.LIST.from(ConfigAdapter.ENUM.from(Waila.Type::class.java)))

    @JvmStatic
    val defaultType = config.getOrThrow("waila.default-type", ConfigAdapter.ENUM.from(Waila.Type::class.java)).apply {
        if (!enabledTypes.contains(this)) {
            throw IllegalStateException("Default Waila type $this is not in the list of enabled types: $enabledTypes")
        }
    }

    @JvmStatic
    val allowedBossBarColors = config.getOrThrow("waila.bossbar.allowed-colors", ConfigAdapter.SET.from(ConfigAdapter.ENUM.from(BossBar.Color::class.java)))

    @JvmStatic
    val allowedBossBarOverlays = config.getOrThrow("waila.bossbar.allowed-overlays", ConfigAdapter.SET.from(ConfigAdapter.ENUM.from(BossBar.Overlay::class.java)))

    @JvmStatic
    val defaultDisplay = config.getOrThrow("waila.default-display.bossbar", ConfigAdapter.WAILA_DISPLAY).apply {
        if (!allowedBossBarColors.contains(color)) {
            throw IllegalStateException("Default bossbar color $color is not in the list of allowed colors: $allowedBossBarColors")
        }
        if (!allowedBossBarOverlays.contains(overlay)) {
            throw IllegalStateException("Default bossbar overlay $overlay is not in the list of allowed overlays: $allowedBossBarOverlays")
        }
    }
}