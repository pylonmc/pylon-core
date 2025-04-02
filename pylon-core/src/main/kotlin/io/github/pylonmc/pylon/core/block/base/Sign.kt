package io.github.pylonmc.pylon.core.block.base

import io.papermc.paper.event.player.PlayerOpenSignEvent
import org.bukkit.event.block.SignChangeEvent

interface Sign {
    fun onSignChange(event: SignChangeEvent) {}
    fun onOpen(event: PlayerOpenSignEvent) {}
}