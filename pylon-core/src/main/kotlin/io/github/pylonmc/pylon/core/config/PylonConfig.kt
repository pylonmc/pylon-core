package io.github.pylonmc.pylon.core.config

import io.github.pylonmc.pylon.core.PylonCore

object PylonConfig {

    private val config = Config(PylonCore, "config.yml")

    @JvmStatic
    val tickRate: Int by config

    @JvmStatic
    val allowedBlockErrors: Int by config

    @JvmStatic
    val wailaIntervalTicks: Int by config

    @JvmStatic
    val fluidIntervalTicks: Int by config

    @JvmStatic
    val researchesEnabled: Boolean = config.getOrThrow("research.enabled")

    @JvmStatic
    val researchCheckInterval: Int = config.getOrThrow("research.interval")

    @JvmStatic
    val translationWrapLimit: Int by config
}