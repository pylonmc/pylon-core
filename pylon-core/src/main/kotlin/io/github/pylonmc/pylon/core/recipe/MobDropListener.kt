package io.github.pylonmc.pylon.core.recipe

import io.github.pylonmc.pylon.core.registry.PylonRegistry
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent

internal object MobDropListener : Listener {

    @EventHandler
    @Suppress("UnstableApiUsage") // DamageSource is unstable
    private fun onMobDrop(event: EntityDeathEvent) {
        val entity = event.entity
        val playerKill = event.damageSource.causingEntity is Player

        for (drop in PylonRegistry.MOB_DROPS) {
            drop.getResult(entity, playerKill)?.let { item ->
                event.drops.add(item)
            }
        }
    }
}