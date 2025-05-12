package io.github.pylonmc.pylon.core.config

import io.github.pylonmc.pylon.core.pluginInstance

object PylonConfig {

    private val config = Config(pluginInstance, "config.yml")

    @JvmStatic
    val tickRate: Int by config

    @JvmStatic
    val allowedBlockErrors: Int by config

    @JvmStatic
    val waliaIntervalTicks: Int by config

    @JvmStatic
    val fluidIntervalTicks: Int by config

    @JvmStatic
    val researchesEnabled: Boolean = config.getOrThrow("research.enabled")

    @JvmStatic
    val researchCheckInterval: Int = config.getOrThrow("research.interval")
}