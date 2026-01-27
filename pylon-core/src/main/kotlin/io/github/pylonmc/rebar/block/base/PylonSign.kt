package io.github.pylonmc.rebar.block.base

import io.papermc.paper.event.player.PlayerOpenSignEvent
import org.bukkit.event.block.SignChangeEvent

interface PylonSign {
    fun onSignChange(event: SignChangeEvent) {}
    fun onOpen(event: PlayerOpenSignEvent) {}
}