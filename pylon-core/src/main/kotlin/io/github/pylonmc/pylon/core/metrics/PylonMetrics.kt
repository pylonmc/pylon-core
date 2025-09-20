package io.github.pylonmc.pylon.core.metrics

import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.config.Config
import io.github.pylonmc.pylon.core.config.PylonConfig
import io.github.pylonmc.pylon.core.config.adapter.ConfigAdapter
import io.github.pylonmc.pylon.core.item.research.Research
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import org.bstats.bukkit.Metrics
import org.bstats.charts.AdvancedPie
import org.bstats.charts.SimplePie
import org.bukkit.Bukkit
import java.util.concurrent.ConcurrentHashMap

internal object PylonMetrics {
    val metrics = Metrics(PylonCore, 27322)
    val dataConfig = Config(PylonCore, "data/metrics.yml")

    var commandsRun = ConcurrentHashMap<String, Int>()
    init {
        val dataSection = dataConfig.getSection("commandsRun")
        if (dataSection != null) {
            for (key in dataSection.keys) {
                commandsRun.put(key, dataSection.getOrThrow(key, ConfigAdapter.INT))
            }
        }
    }

    init {
        metrics.addCustomChart(AdvancedPie("addons") {
            val values = mutableMapOf<String, Int>()
            for (addon in PylonRegistry.ADDONS) {
                values.put(addon.javaClass.simpleName, 1)
            }
            values
        })

        metrics.addCustomChart(AdvancedPie("number_of_addons") {
            mutableMapOf<String, Int>(PylonRegistry.ADDONS.count().toString() to 1)
        })

        metrics.addCustomChart(AdvancedPie("disabled_items") {
            val values = mutableMapOf<String, Int>()
            for (item in PylonConfig.disabledItems) {
                values.put(item.toString(), 1)
            }
            values
        })

        metrics.addCustomChart(SimplePie("researches_enabled") {
            if (PylonConfig.researchesEnabled) { "yes" } else { "no" }
        })

        metrics.addCustomChart(AdvancedPie("researches_unlocked") {
            val researches = mutableMapOf<String, Int>()
            for (player in Bukkit.getOfflinePlayers()) {
                for (research in Research.getResearches(player)) {
                    researches[research.key.toString()] = researches.getOrDefault(research.key.toString(), 0) + 1
                }
            }
            researches
        })

        metrics.addCustomChart(AdvancedPie("commands_run") {
            commandsRun
        })

        Bukkit.getScheduler().runTaskTimerAsynchronously(PylonCore, Runnable { save() }, 0L, PylonConfig.metricsSaveIntervalTicks)
    }

    fun save() {
        val dataSection = dataConfig.getSection("commandsRun") ?: dataConfig.createSection("commandsRun")
        for ((command, runs) in commandsRun) {
            dataSection.set(command, runs)
        }
        dataConfig.save()
    }

    fun onCommandRun(name: String) {
        commandsRun.put(name, commandsRun.getOrDefault(name, 0) + 1)
    }
}