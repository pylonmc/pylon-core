package io.github.pylonmc.pylon.core.guide.pages.base

import com.aallam.similarity.JaroWinkler
import io.github.pylonmc.pylon.core.guide.PylonGuide
import io.github.pylonmc.pylon.core.guide.button.BackButton
import io.github.pylonmc.pylon.core.guide.button.FluidButton
import io.github.pylonmc.pylon.core.i18n.AddonTranslator
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.gui.GuiItems
import io.github.pylonmc.pylon.core.util.key.getAddon
import io.github.pylonmc.pylon.core.util.pylonKey
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.window.AnvilWindow

abstract class SearchPage (key: NamespacedKey, material: Material) : SimpleStaticGuidePage(key, material) {

    abstract fun getItemNamePairs(player: Player, search: String): List<Pair<Item, String>>

    override fun open(player: Player) {
        val lowerGui = PagedGui.items()
            .setStructure(
                "x x x x x x x x x",
                "x x x x x x x x x",
                "x x x x x x x x x",
                "# # # # B # # # #"
            )
            .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            .addIngredient('#', GuiItems.background())
            .addIngredient('B', BackButton(player))
            .build()
        val upperGui = Gui.empty(3, 1)
        upperGui.background = GuiItems.background().itemProvider

        try {
            AnvilWindow.split()
                .setViewer(player)
                .setUpperGui(upperGui)
                .setLowerGui(lowerGui)
                .setTitle(AdventureComponentWrapper(title))
                .addRenameHandler { search -> lowerGui.setContent(getItems(player, search.lowercase())) }
                .open(player)
            PylonGuide.Companion.history.getOrPut(player.uniqueId) { mutableListOf() }.add(this)
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    fun getItems(player: Player, search: String): List<Item> = getItemNamePairs(player, search).map {
        val name = it.second
        val distance = searchAlgorithm.distance(name, search)
        val weight = when {
            name == search -> 0.0
            name.startsWith(search) -> 1.0 + distance
            name.contains(search) -> 2.0 + distance
            else -> 3.0 + distance
        }
        Pair(it.first, weight.takeIf { weight < 3.15 })
    }.filter {
        it.second != null
    }.sortedBy {
        it.second
    }.map {
        it.first
    }
    .take(27)
    .toList()

    companion object {
        val serializer = PlainTextComponentSerializer.plainText()
        val searchAlgorithm = JaroWinkler()
    }
}