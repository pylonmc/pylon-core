package io.github.pylonmc.pylon.core.item.interfaces

import org.bukkit.event.entity.EntityInteractEvent

@FunctionalInterface
interface EntityInteractor {
    /**
     * Called when a player right clicks an entity while holding the item
     */
    fun onUsedToRightClickEntity(event: EntityInteractEvent)
}