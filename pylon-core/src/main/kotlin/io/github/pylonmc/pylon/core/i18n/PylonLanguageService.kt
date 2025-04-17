package io.github.pylonmc.pylon.core.i18n

import io.github.pylonmc.pylon.core.addon.PylonAddon
import io.github.pylonmc.pylon.core.nms.NmsAccessor
import net.kyori.adventure.translation.GlobalTranslator
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerLocaleChangeEvent
import org.bukkit.event.player.PlayerQuitEvent

object PylonLanguageService : Listener {

    private val translators = mutableMapOf<PylonAddon, AddonTranslator>()

    @JvmSynthetic
    internal fun register(addon: PylonAddon) {
        val translator = AddonTranslator(addon)
        GlobalTranslator.translator().addSource(translator)
        translators[addon] = translator
    }

    @JvmSynthetic
    internal fun unregister(addon: PylonAddon) {
        translators.remove(addon)?.let {
            GlobalTranslator.translator().removeSource(it)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        NmsAccessor.instance.register(player, PlayerTranslationHandler(player))
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        NmsAccessor.instance.unregister(player)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPlayerChangeLanguage(event: PlayerLocaleChangeEvent) {
        NmsAccessor.instance.resendInventory(event.player)
    }
}