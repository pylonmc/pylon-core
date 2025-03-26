package io.github.pylonmc.pylon.core.config

import io.github.pylonmc.pylon.core.pluginInstance

object PylonConfig {

    private val config = Config(pluginInstance, "config.yml")

    @JvmField
    val tickDelay = config.get("tick-delay", 10)

    @JvmField
    val allowedBlockErrors = config.get("tick-delay", 10)

    @JvmField
    val itemTickRate = config.get("item-tick-rate", 15L)
}