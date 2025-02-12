package io.github.pylonmc.pylon.core.recipe

import io.github.pylonmc.pylon.core.registry.PylonRegistry
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent

internal object MobDropListener : Listener {

    @EventHandler
    private fun onMobDrop(event: EntityDeathEvent) {
        for (drop in PylonRegistry.MOB_DROPS) {
            drop.getResult(event)?.let { item ->
                event.drops.add(item)
            }
        }
    }
}