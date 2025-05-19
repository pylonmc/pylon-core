package io.github.pylonmc.pylon.core.config

import io.github.pylonmc.pylon.core.PylonCore

object PylonConfig {

    private val config = Config(PylonCore, "config.yml")

    @JvmStatic
    val tickDelay: Int by config

    @JvmStatic
    val allowedBlockErrors: Int by config

    @JvmStatic
    val wailaInterval: Int by config

    @JvmStatic
    val researchesEnabled: Boolean = config.getOrThrow("research.enabled")

    @JvmStatic
    val researchCheckInterval: Int = config.getOrThrow("research.interval")

    @JvmStatic
    val translationWrapLimit: Int by config
}