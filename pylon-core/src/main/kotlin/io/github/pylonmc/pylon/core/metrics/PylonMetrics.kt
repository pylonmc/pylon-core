package io.github.pylonmc.pylon.core.metrics

import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import org.bstats.bukkit.Metrics
import org.bstats.charts.AdvancedPie

object PylonMetrics {
    val metrics: Metrics = Metrics(PylonCore, 27322)

    fun init() {
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


    }
}