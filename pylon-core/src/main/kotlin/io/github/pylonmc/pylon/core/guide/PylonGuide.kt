package io.github.pylonmc.pylon.core.guide

import io.github.pylonmc.pylon.core.guide.pages.FluidsPage
import io.github.pylonmc.pylon.core.guide.pages.ResearchesPage
import io.github.pylonmc.pylon.core.guide.pages.RootPage
import io.github.pylonmc.pylon.core.guide.pages.SearchItemsPage
import io.github.pylonmc.pylon.core.guide.pages.SettingsAndInfoPage
import io.github.pylonmc.pylon.core.guide.pages.InfoPage
import io.github.pylonmc.pylon.core.guide.pages.SearchFluidsPage
import io.github.pylonmc.pylon.core.guide.pages.base.GuidePage
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.item.base.Interactor
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import java.util.UUID

class PylonGuide(stack: ItemStack) : PylonItem(stack), Interactor {

    override fun onUsedToRightClick(event: PlayerInteractEvent) {
        if (event.action.isRightClick) {
            open(event.player)
        }
    }

    companion object {

        @JvmField
        val KEY = pylonKey("guide")

        @JvmField
        val STACK = ItemStackBuilder.pylonItem(Material.ENCHANTED_BOOK, KEY)
            .build()

        @JvmField
        val history: MutableMap<UUID, MutableList<GuidePage>> = mutableMapOf()

        /**
         * We use get() here to force the page to be re-created every time
         *
         * This makes sure the guide is always up to date
         */

        @JvmField
        var fluidsPage = FluidsPage()

        @JvmField
        var infoPage = InfoPage()

        @JvmField
        var researchesPage = ResearchesPage()

        @JvmField
        var rootPage = RootPage()

        @JvmField
        var searchFluidsPage = SearchFluidsPage()

        @JvmField
        var searchItemsPage = SearchItemsPage()

        @JvmField
        var settingsAndInfoPage = SettingsAndInfoPage()

        fun open(player: Player) {
            val history = history.getOrPut(player.uniqueId) { mutableListOf() }
            if (history.isEmpty()) {
                rootPage.open(player)
            } else {
                history.removeLast().open(player)
            }
        }
    }
}