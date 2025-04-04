package io.github.pylonmc.pylon.core.i18n.packet

import io.github.pylonmc.pylon.core.i18n.PlayerTranslationHandler
import org.bukkit.entity.Player
import org.jetbrains.annotations.ApiStatus

@ApiStatus.NonExtendable
interface PacketManager {

    fun register(player: Player, handler: PlayerTranslationHandler)

    fun unregister(player: Player)

    fun resendInventory(player: Player)

    companion object {
        val instance = Class.forName("io.github.pylonmc.pylon.core.i18n.packet.PacketManagerImpl")
            .getDeclaredField("INSTANCE")
            .get(null) as PacketManager
    }
}