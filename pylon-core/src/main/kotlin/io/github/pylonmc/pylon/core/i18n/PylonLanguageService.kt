package io.github.pylonmc.pylon.core.i18n

import io.github.pylonmc.pylon.core.addon.PylonAddon
import io.github.pylonmc.pylon.core.i18n.packet.PacketManager
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.item.PylonItemSchema
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.translation.GlobalTranslator
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerLocaleChangeEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.WeakHashMap

@Suppress("UnstableApiUsage")
object PylonLanguageService : Listener {

    private val translators = mutableMapOf<PylonAddon, AddonTranslator>()

    private val names = WeakHashMap<PylonItemSchema, Component>()
    private val lores = WeakHashMap<PylonItemSchema, ItemLore>()

    fun reset(item: PylonItem<*>) {
        val name = names.getOrPut(item.schema) {
            item.schema.itemStack.getData(DataComponentTypes.ITEM_NAME)!!
        }
        val lore = lores.getOrPut(item.schema) {
            item.schema.itemStack.getData(DataComponentTypes.LORE)!!
        }
        item.stack.setData(DataComponentTypes.ITEM_NAME, name)
        item.stack.setData(DataComponentTypes.LORE, lore)
    }

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
        PacketManager.instance.register(player, PlayerTranslationHandler(player))
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        PacketManager.instance.unregister(player)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPlayerChangeLanguage(event: PlayerLocaleChangeEvent) {
        PacketManager.instance.resendInventory(event.player)
    }
}