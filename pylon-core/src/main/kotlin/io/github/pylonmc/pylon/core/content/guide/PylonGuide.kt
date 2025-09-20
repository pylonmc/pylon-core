package io.github.pylonmc.pylon.core.content.guide

import io.github.pylonmc.pylon.core.guide.pages.InfoPage
import io.github.pylonmc.pylon.core.guide.pages.RootPage
import io.github.pylonmc.pylon.core.guide.pages.SearchItemsAndFluidsPage
import io.github.pylonmc.pylon.core.guide.pages.SettingsAndInfoPage
import io.github.pylonmc.pylon.core.guide.pages.base.GuidePage
import io.github.pylonmc.pylon.core.guide.pages.fluid.FluidsPage
import io.github.pylonmc.pylon.core.guide.pages.item.ItemIngredientsPage
import io.github.pylonmc.pylon.core.guide.pages.research.ResearchesPage
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.item.base.PylonInteractor
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import java.util.UUID

class PylonGuide(stack: ItemStack) : PylonItem(stack), PylonInteractor {

    override fun onUsedToRightClick(event: PlayerInteractEvent) {
        if (event.action.isRightClick) {
            open(event.player)
            event.isCancelled = true
        }
    }

    companion object {

        @JvmField
        val KEY = pylonKey("guide")

        @JvmField
        val STACK = ItemStackBuilder.pylonItem(Material.KNOWLEDGE_BOOK, KEY)
            .build()

        /**
         * Keeps track of the pages the player last visited
         * Resets when the player ends up on the root page
         */
        @JvmStatic
        val history: MutableMap<UUID, MutableList<GuidePage>> = mutableMapOf()

        /**
         * Hidden items do not show up in searches
         */
        @JvmStatic
        val hiddenItems: MutableSet<NamespacedKey> = mutableSetOf()

        /**
         * Hidden fluids do not show up in searches
         */
        @JvmStatic
        val hiddenFluids: MutableSet<NamespacedKey> = mutableSetOf()

        /**
         * Hidden researches do not show up in the researches category
         */
        @JvmStatic
        val hiddenResearches: MutableSet<NamespacedKey> = mutableSetOf()

        @JvmStatic
        val fluidsPage = FluidsPage()

        @JvmStatic
        val infoPage = InfoPage()

        @JvmStatic
        val researchesPage = ResearchesPage()

        @JvmStatic
        val rootPage = RootPage()

        @JvmStatic
        val searchItemsAndFluidsPage = SearchItemsAndFluidsPage()

        @JvmStatic
        val settingsAndInfoPage = SettingsAndInfoPage()

        @JvmStatic
        fun ingredientsPage(stack: ItemStack) = ItemIngredientsPage(stack)

        /**
         * Hide an item from showing up in searches
         */
        @JvmStatic
        fun hideItem(key: NamespacedKey) {
            hiddenItems.add(key)
        }

        /**
         * Hide a fluid from showing up in searches
         */
        @JvmStatic
        fun hideFluid(key: NamespacedKey) {
            hiddenFluids.add(key)
        }

        /**
         * Hide a fluid from showing up in searches
         */
        @JvmStatic
        fun hideResearch(key: NamespacedKey) {
            hiddenResearches.add(key)
        }

        /**
         * Opens the guide to the last page that the player was on
         */
        @JvmStatic
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