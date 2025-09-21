package io.github.pylonmc.pylon.core.entity.base

import org.bukkit.event.entity.EntityDismountEvent
import org.bukkit.event.entity.EntityMountEvent

interface PylonMountingEntity {
    fun onMount(event: EntityMountEvent){}
    fun onDismount(event: EntityDismountEvent){}
}