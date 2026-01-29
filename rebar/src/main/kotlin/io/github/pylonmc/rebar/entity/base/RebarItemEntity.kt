package io.github.pylonmc.rebar.entity.base

import org.bukkit.event.entity.ItemDespawnEvent
import org.bukkit.event.entity.ItemMergeEvent

interface RebarItemEntity {
    fun onDespawn(event: ItemDespawnEvent) {}
    fun onMerge(event: ItemMergeEvent) {}
}