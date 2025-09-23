package io.github.pylonmc.pylon.core.guide.pages.base

import info.debatty.java.stringsimilarity.JaroWinkler
import io.github.pylonmc.pylon.core.addon.PylonAddon
import io.github.pylonmc.pylon.core.content.guide.PylonGuide
import io.github.pylonmc.pylon.core.guide.button.BackButton
import io.github.pylonmc.pylon.core.guide.button.FluidButton
import io.github.pylonmc.pylon.core.guide.button.ItemButton
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.gui.GuiItems
import io.github.pylonmc.pylon.core.util.plainText
import io.github.pylonmc.pylon.core.util.render
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.window.AnvilWindow
import java.util.UUID

abstract class SearchPage (key: NamespacedKey, material: Material) : SimpleStaticGuidePage(key, material) {

    abstract fun getItemNamePairs(player: Player, search: String): List<Pair<Item, String>>

    override fun open(player: Player) {
        var dummyRename = true
        val search = searches.getOrDefault(player.uniqueId, "")
        val lowerGui = PagedGui.items()
            .setStructure(
                "x x x x x x x x x",
                "x x x x x x x x x",
                "x x x x x x x x x",
                "< # # # B # # # >"
            )
            .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            .addIngredient('#', GuiItems.background())
            .addIngredient('B', BackButton())
            .addIngredient('<', GuiItems.pagePrevious())
            .addIngredient('>', GuiItems.pageNext())
            .setContent(getItems(player, search))
            .build()
        val upperGui = Gui.normal()
            .setStructure("# # #")
            .addIngredient('#', GuiItems.background(search))
            .build()

        try {
            AnvilWindow.split()
                .setViewer(player)
                .setUpperGui(upperGui)
                .setLowerGui(lowerGui)
                .setTitle(AdventureComponentWrapper(title))
                .addRenameHandler { search ->
                    if (dummyRename) {
                        dummyRename = false
                        return@addRenameHandler
                    }

                    try {
                        searches[player.uniqueId] = search.lowercase()
                        lowerGui.setContent(getItems(player, search.lowercase()))
                    } catch (t: Throwable) { // If uncaught, will crash the server
                        t.printStackTrace()
                    }
                }
                .open(player)
            PylonGuide.history.getOrPut(player.uniqueId) { mutableListOf() }.add(this)
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    open fun getItems(player: Player, search: String): List<Item> {
        val terms = mutableListOf<SearchTerm>()
        val split = search.split(" ")
        val remainder = StringBuilder()
        fun flushRemainder() {
            if (remainder.isEmpty()) return
            terms.add { entry ->
                weight(remainder.toString(), entry.second)
            }
        }

        for (piece in split) {
            if (piece.isEmpty()) continue
            val type = piece[0]
            when(type) {
                '@' -> {
                    flushRemainder()
                    terms.add { entry ->
                        val item = entry.first
                        lateinit var key: NamespacedKey
                        if (item is FluidButton) {
                            key = item.currentFluid.key
                        } else {
                            key = PylonItem.fromStack(item.getItemProvider(player).get())?.key ?: return@add null
                        }
                        return@add if (key.namespace.startsWith(piece.substring(1), ignoreCase = true)) 0.0 else null
                    }
                }
                '$' -> {
                    flushRemainder()
                    terms.add { entry ->
                        val stack = entry.first.getItemProvider(player).get()
                        stack.lore()?.map {
                            weight(piece.substring(1), it.render(player.locale()).plainText.lowercase())
                        }?.filterNotNull()?.minOrNull()
                    }
                }
                else -> {
                    if (remainder.isNotEmpty()) remainder.append(" ")
                    remainder.append(piece)
                }
            }
        }
        flushRemainder()

        val entries = getItemNamePairs(player, search).toMutableList()
        for (term in terms) {
            val weighted = entries.mapNotNull { entry ->
                val weight = term.weight(entry) ?: return@mapNotNull null
                Pair(entry.first, weight)
            }
            entries.removeIf { entry ->
                weighted.none { it.first == entry.first }
            }
            entries.sortBy { entry ->
                weighted.first { it.first == entry.first }.second
            }
        }
        return entries.map { it.first }.toList()
    }

    companion object {
        private val searchAlgorithm = JaroWinkler()
        private val searches = mutableMapOf<UUID, String>()

        private fun weight(search: String, text: String): Double? {
            val distance = searchAlgorithm.distance(text, search)
            val weight = when {
                text == search -> 0.0
                text.startsWith(search) -> 1.0 + distance
                text.contains(search) -> 2.0 + distance
                else -> 3.0 + distance
            }
            return weight.takeIf { weight < 3.15 }
        }
    }

    private fun interface SearchTerm {
        fun weight(entry: Pair<Item, String>): Double?
    }
}