package io.github.pylonmc.rebar.electricity

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*

object WireConnectionService : Listener {

    private val connecting = mutableMapOf<UUID, WireEntity>()

    fun getWirePlayerIsConnecting(player: Player) = connecting[player.uniqueId]

    fun stopConnectingWire(player: Player) {
        connecting.remove(player.uniqueId)?.remove()
    }

    @EventHandler
    private fun onPlayerMove(event: PlayerMoveEvent) {
        if (!event.hasChangedPosition()) return
        getWirePlayerIsConnecting(event.player)?.updateTransformation()
    }

    @EventHandler
    private fun onPlayerLeave(event: PlayerQuitEvent) {
        stopConnectingWire(event.player)
    }

    @EventHandler
    private fun onPlayerChangeWorld(event: PlayerChangedWorldEvent) {
        stopConnectingWire(event.player)
    }
}