package io.github.pylonmc.pylon.core.item.interfaces

import org.bukkit.event.player.PlayerInteractEvent

@FunctionalInterface
interface BlockInteractor {
    /**
     * Called when a player right clicks a block while holding the item
     */
    fun onUsedToRightClickBlock(event: PlayerInteractEvent)
}