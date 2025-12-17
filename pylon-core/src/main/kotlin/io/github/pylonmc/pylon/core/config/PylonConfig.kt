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

    @JvmField
    val pylonGuideOnFirstJoin = config.getOrThrow("pylon-guide-on-first-join", ConfigAdapter.BOOLEAN)

    @JvmField
    val defaultTickInterval = config.getOrThrow("default-tick-interval", ConfigAdapter.INT)

    @JvmField
    val allowedBlockErrors = config.getOrThrow("allowed-block-errors", ConfigAdapter.INT)

    @JvmField
    val allowedEntityErrors = config.getOrThrow("allowed-entity-errors", ConfigAdapter.INT)

    @JvmField
    val fluidTickInterval = config.getOrThrow("fluid-tick-interval", ConfigAdapter.INT)

    @JvmField
    val blockDataAutosaveIntervalSeconds = config.getOrThrow("block-data-autosave-interval-seconds", ConfigAdapter.LONG)

    @JvmField
    val entityDataAutosaveIntervalSeconds = config.getOrThrow("entity-data-autosave-interval-seconds", ConfigAdapter.LONG)

    @JvmField
    val researchesEnabled = config.getOrThrow("research.enabled", ConfigAdapter.BOOLEAN)

    @JvmField
    val researchBaseConfettiAmount = config.get("research.confetti.base-amount", ConfigAdapter.DOUBLE, 70.0)

    @JvmField
    val researchMultiplierConfettiAmount = config.get("research.confetti.multiplier", ConfigAdapter.DOUBLE, 0.2)

    @JvmField
    val researchMaxConfettiAmount = config.get("research.confetti.max-amount", ConfigAdapter.INT, 700)

    @JvmField
    val researchSounds = config.getOrThrow("research.sounds", ConfigAdapter.MAP.from(ConfigAdapter.LONG, ConfigAdapter.RANDOMIZED_SOUND))

    @JvmField
    val pipePlacementTaskIntervalTicks = config.getOrThrow("pipe-placement.tick-interval", ConfigAdapter.LONG)

    @JvmField
    val pipePlacementMaxLength = config.getOrThrow("pipe-placement.max-length", ConfigAdapter.LONG)

    @JvmField
    val pipePlacementCancelDistance = config.getOrThrow("pipe-placement.cancel-distance", ConfigAdapter.INT)

    @JvmField
    val translationWrapLimit = config.getOrThrow("translation-wrap-limit", ConfigAdapter.INT)

    @JvmField
    val metricsSaveIntervalTicks = config.getOrThrow("metrics-save-interval-ticks", ConfigAdapter.LONG)

    @JvmField
    val disabledItems = config.getOrThrow("disabled-items", ConfigAdapter.SET.from(ConfigAdapter.NAMESPACED_KEY))

    @JvmField
    val inventoryTickerBaseRate = config.getOrThrow("inventory-ticker-base-rate", ConfigAdapter.LONG)

    @JvmField
    val cargoTickInterval = config.getOrThrow("cargo-tick-interval", ConfigAdapter.INT)

    @JvmField
    val cargoTransferRateMultiplier = config.getOrThrow("cargo-transfer-rate-multiplier", ConfigAdapter.INT)

    object WailaConfig {
        private val config = Config(PylonCore, "config.yml")

        @JvmStatic
        val enabled
            get() = tickInterval > 0 && enabledTypes.isNotEmpty()

        @JvmField
        val tickInterval = config.getOrThrow("waila.tick-interval", ConfigAdapter.INT)

        @JvmField
        val enabledTypes = config.getOrThrow("waila.enabled-types", ConfigAdapter.LIST.from(ConfigAdapter.ENUM.from(Waila.Type::class.java)))

        @JvmField
        val defaultType = config.getOrThrow("waila.default-type", ConfigAdapter.ENUM.from(Waila.Type::class.java)).apply {
            if (!enabledTypes.contains(this)) {
                throw IllegalStateException("Default Waila type $this is not in the list of enabled types: $enabledTypes")
            }
        }

        @JvmField
        val allowedBossBarColors = config.getOrThrow("waila.bossbar.allowed-colors", ConfigAdapter.SET.from(ConfigAdapter.ENUM.from(BossBar.Color::class.java)))

        @JvmField
        val allowedBossBarOverlays = config.getOrThrow("waila.bossbar.allowed-overlays", ConfigAdapter.SET.from(ConfigAdapter.ENUM.from(BossBar.Overlay::class.java)))

        @JvmField
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

        @JvmField
        val enabled = config.getOrThrow("custom-armor-textures.enabled", ConfigAdapter.BOOLEAN)

        @JvmField
        val forced = config.getOrThrow("custom-armor-textures.force", ConfigAdapter.BOOLEAN)

    }

    object BlockTextureConfig {

        private val config = Config(PylonCore, "config.yml")

        @JvmField
        val enabled = config.getOrThrow("custom-block-textures.enabled", ConfigAdapter.BOOLEAN)

        @JvmField
        val default = config.getOrThrow("custom-block-textures.default", ConfigAdapter.BOOLEAN)

        @JvmField
        val forced = config.getOrThrow("custom-block-textures.force", ConfigAdapter.BOOLEAN)

        @JvmField
        val occludingCacheRefreshInterval = config.getOrThrow("custom-block-textures.culling.occluding-cache-refresh-interval", ConfigAdapter.INT)

        @JvmField
        val occludingCacheRefreshShare = config.getOrThrow("custom-block-textures.culling.occluding-cache-refresh-share", ConfigAdapter.DOUBLE)

        @JvmField
        val cullingPresets = config.getOrThrow("custom-block-textures.culling.presets", ConfigAdapter.MAP.from(ConfigAdapter.STRING, ConfigAdapter.CULLING_PRESET))

        @JvmField
        val defaultCullingPreset = run {
            val key = config.getOrThrow<String>("custom-block-textures.culling.default-preset", ConfigAdapter.STRING)
            cullingPresets[key] ?: error("No culling preset with id '$key' found")
        }

    }

}