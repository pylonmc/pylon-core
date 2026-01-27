package io.github.pylonmc.rebar.entity.base

import org.bukkit.event.entity.EntityDismountEvent
import org.bukkit.event.entity.EntityMountEvent

interface RebarMountingEntity {
    fun onMount(event: EntityMountEvent) {}
    fun onDismount(event: EntityDismountEvent) {}
}