package io.github.pylonmc.pylon.core.i18n

import io.github.pylonmc.pylon.core.i18n.packet.PacketManager
import io.github.pylonmc.pylon.core.item.PylonItem
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.text.Component
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import kotlin.random.Random

internal object PlayerConnectionListener : Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        PacketManager.instance.register(player, object : PlayerTranslationHandler(player) {
            override fun handleItem(item: PylonItem<*>) {
                item.stack.setData(DataComponentTypes.ITEM_NAME, Component.text(Random.nextInt()))
            }
        })
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        PacketManager.instance.unregister(player)
    }
}