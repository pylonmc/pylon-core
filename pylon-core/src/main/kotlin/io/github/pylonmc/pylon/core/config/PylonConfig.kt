package io.github.pylonmc.pylon.core.config

import io.github.pylonmc.pylon.core.pluginInstance

object PylonConfig {

    private val config = Config(pluginInstance, "config.yml")

    @JvmStatic
    val tickDelay: Int by config

    @JvmStatic
    val allowedBlockErrors: Int by config

    @JvmStatic
    val waliaInterval: Int by config

    @JvmStatic
    val researchesEnabled: Boolean by config
}