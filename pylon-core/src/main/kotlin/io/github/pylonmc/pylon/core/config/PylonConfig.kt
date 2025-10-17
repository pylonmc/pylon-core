package io.github.pylonmc.pylon.core.config

import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.config.adapter.ConfigAdapter
import io.github.pylonmc.pylon.core.waila.Waila
import net.kyori.adventure.bossbar.BossBar

/**
 * The config options for Pylon Core.
 */
object PylonConfig {

    private val config = Config(PylonCore, "config.yml")

    @JvmStatic
    val pylonGuideOnFirstJoin = config.getOrThrow("pylon-guide-on-first-join", ConfigAdapter.BOOLEAN)

    @JvmStatic
    val defaultTickInterval = config.getOrThrow("default-tick-interval", ConfigAdapter.INT)

    @JvmStatic
    val allowedBlockErrors = config.getOrThrow("allowed-block-errors", ConfigAdapter.INT)

    @JvmStatic
    val allowedEntityErrors = config.getOrThrow("allowed-entity-errors", ConfigAdapter.INT)

    @JvmStatic
    val fluidTickInterval = config.getOrThrow("fluid-tick-interval", ConfigAdapter.INT)

    @JvmStatic
    val blockDataAutosaveIntervalSeconds = config.getOrThrow("block-data-autosave-interval-seconds", ConfigAdapter.LONG)

    @JvmStatic
    val entityDataAutosaveIntervalSeconds = config.getOrThrow("entity-data-autosave-interval-seconds", ConfigAdapter.LONG)

    @JvmStatic
    val researchesEnabled = config.getOrThrow("research.enabled", ConfigAdapter.BOOLEAN)

    @JvmStatic
    val researchBaseConfettiAmount = config.get("research.confetti.base-amount", ConfigAdapter.DOUBLE, 70.0)

    @JvmStatic
    val researchMultiplierConfettiAmount = config.get("research.confetti.multiplier", ConfigAdapter.DOUBLE, 0.2)

    @JvmStatic
    val researchMaxConfettiAmount = config.get("research.confetti.max-amount", ConfigAdapter.INT, 700)

    @JvmStatic
    val researchSounds = config.getOrThrow("research.sounds", ConfigAdapter.MAP.from(ConfigAdapter.LONG, ConfigAdapter.RANDOMIZED_SOUND))

    @JvmStatic
    val pipePlacementTaskIntervalTicks = config.getOrThrow("pipe-placement.tick-interval", ConfigAdapter.LONG)

    @JvmStatic
    val pipePlacementMaxDistance = config.getOrThrow("pipe-placement.max-distance", ConfigAdapter.LONG)

    @JvmStatic
    val translationWrapLimit = config.getOrThrow("translation-wrap-limit", ConfigAdapter.INT)

    @JvmStatic
    val metricsSaveIntervalTicks = config.getOrThrow("metrics-save-interval-ticks", ConfigAdapter.LONG)

    @JvmStatic
    val disabledItems = config.getOrThrow("disabled-items", ConfigAdapter.SET.from(ConfigAdapter.NAMESPACED_KEY))

    object WailaConfig {
        private val config = Config(PylonCore, "config.yml")

        @JvmStatic
        val enabled
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

    object ArmorTextureConfig {

        private val config = Config(PylonCore, "config.yml")

        @JvmStatic
        val enabled = config.getOrThrow("custom-armor-textures.enabled", ConfigAdapter.BOOLEAN)

        @JvmStatic
        val forced = config.getOrThrow("custom-armor-textures.force", ConfigAdapter.BOOLEAN)

    }

    object BlockTextureConfig {

        private val config = Config(PylonCore, "config.yml")

        @JvmStatic
        val enabled = config.getOrThrow("custom-block-textures.enabled", ConfigAdapter.BOOLEAN)

        @JvmStatic
        val default = config.getOrThrow("custom-block-textures.default", ConfigAdapter.BOOLEAN)

        @JvmStatic
        val forced = config.getOrThrow("custom-block-textures.force", ConfigAdapter.BOOLEAN)

        @JvmStatic
        val stateUpdateInterval = config.getOrThrow("custom-block-textures.state-update-interval", ConfigAdapter.INT)

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

}