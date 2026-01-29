package io.github.pylonmc.rebar.block.base

import io.papermc.paper.event.player.PlayerInsertLecternBookEvent
import io.papermc.paper.event.player.PlayerLecternPageChangeEvent
import org.bukkit.event.player.PlayerTakeLecternBookEvent

interface RebarLectern {
    fun onInsertBook(event: PlayerInsertLecternBookEvent) {}
    fun onRemoveBook(event: PlayerTakeLecternBookEvent) {}
    fun onChangePage(event: PlayerLecternPageChangeEvent) {}
}