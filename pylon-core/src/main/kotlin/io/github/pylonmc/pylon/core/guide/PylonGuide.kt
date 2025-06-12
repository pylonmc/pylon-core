package io.github.pylonmc.pylon.core.guide

import io.github.pylonmc.pylon.core.guide.pages.FluidsPage
import io.github.pylonmc.pylon.core.guide.pages.ResearchesPage
import io.github.pylonmc.pylon.core.guide.pages.RootPage
import io.github.pylonmc.pylon.core.guide.pages.SearchFluidsPage
import io.github.pylonmc.pylon.core.guide.pages.SearchItemsPage
import io.github.pylonmc.pylon.core.guide.pages.SearchResearchesPage
import io.github.pylonmc.pylon.core.guide.pages.SettingsAndInfoPage
import io.github.pylonmc.pylon.core.guide.pages.InfoPage
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.item.base.Interactor
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.Material
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

class PylonGuide(stack: ItemStack) : PylonItem(stack), Interactor {

    override fun onUsedToRightClick(event: PlayerInteractEvent) {
        if (event.action.isRightClick) {
            rootPage.open(event.player)
        }
    }

    companion object {

        @JvmField
        val KEY = pylonKey("guide")

        @JvmField
        val STACK = ItemStackBuilder.pylonItem(Material.ENCHANTED_BOOK, KEY)
            .build()

        /**
         * We use get() here to force the page to be re-created every time
         *
         * This makes sure the guide is always up to date
         */

        @JvmField
        val fluidsPage = FluidsPage()

        @JvmField
        val infoPage = InfoPage()

        @JvmField
        val researchesPage = ResearchesPage()

        @JvmField
        val rootPage = RootPage()

        @JvmField
        val searchFluidsPage = SearchFluidsPage()

        @JvmField
        val searchItemsPage = SearchItemsPage()

        @JvmField
        val searchResearchesPage = SearchResearchesPage()

        @JvmField
        val settingsAndInfoPage = SettingsAndInfoPage()
    }
}