package io.github.pylonmc.pylon.core.guide.pages.base

import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.PagedGui
import java.util.UUID
import java.util.WeakHashMap

/**
 * Represents a GuidePage with multiple pages
 * When building your [PagedGui] add a page change handler using [saveCurrentPage] to remember page changes
 * After constructing your [PagedGui], use [loadCurrentPage] to set the correct page for the player
 *
 * For example:
 * ```kotlin
 * override fun getGui(player: Player): Gui {
 *    val gui = PagedGui.items()
 *        .setStructure(...)
 *        ...
 *        .addPageChangeHandler { _, newPage -> saveCurrentPage(player, newPage) }
 *    return gui.build().apply { loadCurrentPage(player, this) }
 * }
 * ```
 */
interface PagedGuidePage : GuidePage {
    fun loadCurrentPage(player: Player, gui: PagedGui<*>) {
        gui.setPage(pageNumbers.getOrPut(this) { mutableMapOf() }.getOrDefault(player.uniqueId, 0))
    }

    fun saveCurrentPage(player: Player, page: Int) {
        pageNumbers.getOrPut(this) { mutableMapOf() }[player.uniqueId] = page
    }

    companion object {
        val pageNumbers = WeakHashMap<GuidePage, MutableMap<UUID, Int>>()
    }
}