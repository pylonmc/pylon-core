package io.github.pylonmc.pylon.core.config

import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.config.adapter.ConfigAdapter

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
    val wailaTickInterval = config.getOrThrow("waila-tick-interval", ConfigAdapter.INT)

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

    @JvmStatic
    val inventoryTickerBaseRate = config.getOrThrow("inventory-ticker-base-rate", ConfigAdapter.LONG)
}