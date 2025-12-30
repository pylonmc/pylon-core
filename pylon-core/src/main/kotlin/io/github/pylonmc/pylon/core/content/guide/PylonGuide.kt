package io.github.pylonmc.pylon.core.content.guide

import io.github.pylonmc.pylon.core.config.PylonConfig
import io.github.pylonmc.pylon.core.guide.pages.InfoPage
import io.github.pylonmc.pylon.core.guide.pages.PlayerSettingsPage
import io.github.pylonmc.pylon.core.guide.pages.RootPage
import io.github.pylonmc.pylon.core.guide.pages.SearchItemsAndFluidsPage
import io.github.pylonmc.pylon.core.guide.pages.base.GuidePage
import io.github.pylonmc.pylon.core.guide.pages.fluid.FluidsPage
import io.github.pylonmc.pylon.core.guide.pages.item.ItemIngredientsPage
import io.github.pylonmc.pylon.core.guide.pages.research.ResearchesPage
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.item.base.PylonInteractor
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.util.pylonKey
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.key.Key
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import java.util.UUID

/**
 * The one and only Pylon guide.
 */
class PylonGuide(stack: ItemStack) : PylonItem(stack), PylonInteractor {

    override fun onUsedToRightClick(event: PlayerInteractEvent) {
        if (event.action.isRightClick) {
            open(event.player)
            event.isCancelled = true
        }
    }

    companion object : Listener {

        @JvmField
        val KEY = pylonKey("guide")

        @JvmField
        val STACK = ItemStackBuilder.pylon(Material.BOOK, KEY)
            .set(DataComponentTypes.ITEM_MODEL, Key.key("knowledge_book"))
            .set(DataComponentTypes.MAX_STACK_SIZE, 1)
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
        val settingsPage = PlayerSettingsPage()

        /**
         * Lowest priority to avoid another plugin saving the players data or doing something
         * to make the player considered as having played before, before we receive the event
         */
        @EventHandler(priority = EventPriority.LOWEST)
        private fun join(event: PlayerJoinEvent) {
            if (PylonConfig.PYLON_GUIDE_ON_FIRST_JOIN && !event.player.hasPlayedBefore()) {
                event.player.give(STACK.clone())
            }
        }

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