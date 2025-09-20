package io.github.pylonmc.pylon.core.config

import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.config.adapter.ConfigAdapter

object PylonConfig {

    private val config = Config(PylonCore, "config.yml")

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
    val pipePlacementTaskIntervalTicks = config.getOrThrow("pipe-placement.tick-interval", ConfigAdapter.LONG)

    @JvmStatic
    val pipePlacementMaxDistance = config.getOrThrow("pipe-placement.max-distance", ConfigAdapter.LONG)

    @JvmStatic
    val translationWrapLimit = config.getOrThrow("translation-wrap-limit", ConfigAdapter.INT)

    @JvmStatic
    val metricsSaveIntervalTicks = config.getOrThrow("metrics-save-interval-ticks", ConfigAdapter.LONG)

    @JvmStatic
    val disabledItems = config.getOrThrow("disabled-items", ConfigAdapter.SET.from(ConfigAdapter.NAMESPACED_KEY))
}