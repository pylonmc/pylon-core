package io.github.pylonmc.rebar.electricity

import io.github.pylonmc.rebar.Rebar
import io.github.pylonmc.rebar.i18n.RebarArgument
import io.github.pylonmc.rebar.item.RebarItem
import io.github.pylonmc.rebar.item.interfaces.WireRebarItem
import io.github.pylonmc.rebar.util.delayTicks
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*

object WireConnectionService : Listener {

    private val connecting = mutableMapOf<UUID, WireEntity>()
    private val jobs = mutableMapOf<UUID, Job>()

    @JvmStatic
    fun startConnectingWire(player: Player, wire: WireEntity) {
        connecting[player.uniqueId] = wire
        jobs[player.uniqueId] = Rebar.scope.launch {
            while (true) {
                val total = player.inventory.sumOf { if (RebarItem.isRebarItem<WireRebarItem>(it)) it.amount else 0 }
                val color = if (player.gameMode == GameMode.SURVIVAL && wire.wireCount > total) NamedTextColor.RED else NamedTextColor.GREEN
                player.sendActionBar(
                    Component.translatable(
                        "rebar.message.wiring.wiring",
                        RebarArgument.of("wires", wire.wireCount),
                        RebarArgument.of("total", total)
                    ).color(color)
                )
                delayTicks(10)
            }
        }
        player.sendMessage(Component.translatable("rebar.message.wiring.instructions"))
    }

    @JvmStatic
    fun getWirePlayerIsConnecting(player: Player) = connecting[player.uniqueId]

    @JvmStatic
    @JvmOverloads
    fun stopConnectingWire(player: Player, delete: Boolean = true) {
        val wire = connecting.remove(player.uniqueId)
        if (delete) wire?.remove()
        jobs.remove(player.uniqueId)?.cancel()
        player.sendActionBar(Component.empty())
    }

    @EventHandler
    private fun onPlayerMove(event: PlayerMoveEvent) {
        if (!event.hasChangedPosition()) return
        getWirePlayerIsConnecting(event.player)?.update()
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