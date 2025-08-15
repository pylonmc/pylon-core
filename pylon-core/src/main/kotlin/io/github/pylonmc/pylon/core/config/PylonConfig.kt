package io.github.pylonmc.pylon.core.config

import io.github.pylonmc.pylon.core.PylonCore
import org.bukkit.NamespacedKey

object PylonConfig {

    private val config = Config(PylonCore, "config.yml")

    @JvmStatic
    val defaultTickInterval: Int by config

    @JvmStatic
    val allowedBlockErrors: Int by config

    @JvmStatic
    val allowedEntityErrors: Int by config

    @JvmStatic
    val wailaTickInterval: Int by config

    @JvmStatic
    val fluidTickInterval: Int by config

    @JvmStatic
    val blockDataAutosaveIntervalSeconds: Long = config.getOrThrow<Int>("block-data-autosave-interval-seconds").toLong()

    @JvmStatic
    val entityDataAutosaveIntervalSeconds: Long = config.getOrThrow<Int>("entity-data-autosave-interval-seconds").toLong()

    @JvmStatic
    val researchesEnabled: Boolean = config.getOrThrow("research.enabled")

    val PIPE_PLACEMENT_TASK_INTERVAL_TICKS: Long = config.getOrThrow<Int>("pipe-placement.tick-interval").toLong()

    val PIPE_PLACEMENT_MAX_DISTANCE: Long = config.getOrThrow<Int>("pipe-placement.max-distance").toLong()


    @JvmStatic
    val translationWrapLimit: Int by config

    @JvmStatic
    val disabledItems: Set<NamespacedKey> =
        config.getOrThrow<List<String>>("disabled-items")
            .mapNotNull {
                val key = NamespacedKey.fromString(it)
                if (key == null) {
                    PylonCore.logger.warning { "Invalid disabled-items key '$it'" }
                }
                key
            }
            .toSet()
}