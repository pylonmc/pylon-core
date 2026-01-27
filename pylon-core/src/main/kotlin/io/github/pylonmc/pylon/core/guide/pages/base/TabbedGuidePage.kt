package io.github.pylonmc.pylon.core.guide.pages.base

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import xyz.xenondevs.invui.gui.TabGui
import java.util.*

/**
 * Represents a GuidePage with multiple tabs
 * When building your [TabGui] add a page change handler using [saveCurrentTab] to remember page changes
 * After constructing your [TabGui], use [loadCurrentTab] to set the correct page for the player
 *
 * For example:
 * ```kotlin
 * override fun getGui(player: Player): Gui {
 *    val gui = TabGui.normal()
 *        .setStructure(...)
 *        ...
 *        .addTabChangeHandler { _, newTab -> saveCurrentTab(player, newTab) }
 *    return gui.build().apply { loadCurrentTab(player, this) }
 * }
 * ```
 */
interface TabbedGuidePage : GuidePage {
    fun loadCurrentTab(player: Player, gui: TabGui) {
        gui.setTab(tabNumbers.getOrPut(this, ::mutableMapOf).getOrDefault(player.uniqueId, 0))
    }

    fun saveCurrentTab(player: Player, tab: Int) {
        tabNumbers.getOrPut(this, ::mutableMapOf)[player.uniqueId] = tab
    }

    companion object : Listener {
        val tabNumbers = WeakHashMap<GuidePage, MutableMap<UUID, Int>>()

        @EventHandler
        private fun onPlayerQuit(event: PlayerQuitEvent) {
            val uuid = event.player.uniqueId
            for (map in tabNumbers.values) {
                map.remove(uuid)
            }
        }
    }
}