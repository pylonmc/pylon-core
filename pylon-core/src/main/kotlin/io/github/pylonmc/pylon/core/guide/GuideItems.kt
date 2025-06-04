package io.github.pylonmc.pylon.core.guide

import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.util.pylonKey
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AbstractItem

object GuideItems {

    fun fluids() = object : AbstractItem() {

        val KEY = pylonKey("guide_fluids")

        override fun getItemProvider(): ItemProvider {
            return ItemStackBuilder.pylonItem(Material.WATER_BUCKET, KEY)
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            // TODO fluids page
        }
    }

    fun researches() = object : AbstractItem() {

        val KEY = pylonKey("guide_researches")

        override fun getItemProvider(): ItemProvider {
            return ItemStackBuilder.pylonItem(Material.BREWING_STAND, KEY)
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            // TODO researches page
        }
    }

    fun settingsAndInfo() = object : AbstractItem() {

        val KEY = pylonKey("guide_settings_and_info")

        override fun getItemProvider(): ItemProvider {
            return ItemStackBuilder.pylonItem(Material.TODO, KEY)
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            // TODO researches page
        }
    }

    fun guides() = object : AbstractItem() {

        val KEY = pylonKey("guide_guides")

        override fun getItemProvider(): ItemProvider {
            return ItemStackBuilder.pylonItem(Material.BOOK, KEY)
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            // TODO guides page
        }
    }

    fun searchItems() = object : AbstractItem() {

        val KEY = pylonKey("guide_search_items")

        override fun getItemProvider(): ItemProvider {
            return ItemStackBuilder.pylonItem(Material.OAK_SIGN, KEY)
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            // TODO item search page
        }
    }

    fun searchFluids() = object : AbstractItem() {

        val KEY = pylonKey("guide_search_fluids")

        override fun getItemProvider(): ItemProvider {
            return ItemStackBuilder.pylonItem(Material.THE_BLUE_SIGN_IDK_MY_LSP_ISNT_WORKING, KEY)
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            // TODO fluid search page
        }
    }

    fun searchResearches() = object : AbstractItem() {

        val KEY = pylonKey("guide_search_researches")

        override fun getItemProvider(): ItemProvider {
            return ItemStackBuilder.pylonItem(Material.THE_RED_SIGN_IDK_MY_LSP_ISNT_WORKING, KEY)
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            // TODO research search page
        }
    }
}