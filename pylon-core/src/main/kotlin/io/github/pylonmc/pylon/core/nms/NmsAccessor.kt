package io.github.pylonmc.pylon.core.nms

import io.github.pylonmc.pylon.core.i18n.PlayerTranslationHandler
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataContainer
import org.jetbrains.annotations.ApiStatus

/**
 * Internal, not for innocent eyes to see, move along now.
 */
@ApiStatus.Internal
@ApiStatus.NonExtendable
interface NmsAccessor {

    fun registerTranslationHandler(player: Player, handler: PlayerTranslationHandler)

    fun unregisterTranslationHandler(player: Player)

    fun resendInventory(player: Player)

    fun resendRecipeBook(player: Player)

    fun serializePdc(pdc: PersistentDataContainer): Component

    companion object {
        val instance = Class.forName("io.github.pylonmc.pylon.core.nms.NmsAccessorImpl")
            .getDeclaredField("INSTANCE")
            .get(null) as NmsAccessor
    }
}