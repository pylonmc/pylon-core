package io.github.pylonmc.rebar.item.base

import org.bukkit.event.player.PlayerInteractEvent

interface PylonBlockInteractor : PylonCooldownable {
    /**
     * May be fired twice (once for each hand), and is fired for both left and right clicks.
     */
    fun onUsedToClickBlock(event: PlayerInteractEvent)
}