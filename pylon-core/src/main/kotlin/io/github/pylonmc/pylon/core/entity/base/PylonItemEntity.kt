package io.github.pylonmc.pylon.core.entity.base

import org.bukkit.event.entity.ItemDespawnEvent
import org.bukkit.event.entity.ItemMergeEvent

interface PylonItemEntity {
    fun onDespawn(event: ItemDespawnEvent) {}
    fun onMerge(event: ItemMergeEvent) {}

}