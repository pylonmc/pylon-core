package io.github.pylonmc.pylon.core.guide

import io.github.pylonmc.pylon.core.guide.pages.FluidsPage
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.util.pylonKey
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AbstractItem

object GuideItems {

    fun back() = object : AbstractItem() {

        override fun getItemProvider(): ItemProvider {
            return ItemStackBuilder.of(Material.ENCHANTED_BOOK)
                .name(Component.translatable("pylon.pyloncore.guide.button.back"))
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            // TODO history system
        }
    }

    fun fluids() = object : AbstractItem() {

        override fun getItemProvider(): ItemProvider {
            return ItemStackBuilder.of(Material.WATER_BUCKET)
                .name(Component.translatable("pylon.pyloncore.guide.button.fluids"))
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            FluidsPage.open(player)
        }
    }

    fun researches() = object : AbstractItem() {

        override fun getItemProvider(): ItemProvider {
            return ItemStackBuilder.of(Material.BREWING_STAND)
                .name(Component.translatable("pylon.pyloncore.guide.button.researches"))
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            // TODO researches page
        }
    }

    fun settingsAndInfo() = object : AbstractItem() {

        override fun getItemProvider(): ItemProvider {
            return ItemStackBuilder.of(Material.COMPARATOR)
                .name(Component.translatable("pylon.pyloncore.guide.button.settings-and-info"))
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            // TODO researches page
        }
    }

    fun guides() = object : AbstractItem() {

        override fun getItemProvider(): ItemProvider {
            return ItemStackBuilder.of(Material.LANTERN)
                .name(Component.translatable("pylon.pyloncore.guide.button.guides"))
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            // TODO guides page
        }
    }

    fun searchItems() = object : AbstractItem() {

        override fun getItemProvider(): ItemProvider {
            return ItemStackBuilder.of(Material.OAK_SIGN)
                .name(Component.translatable("pylon.pyloncore.guide.button.search-items"))
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            // TODO item search page
        }
    }

    fun searchFluids() = object : AbstractItem() {

        override fun getItemProvider(): ItemProvider {
            return ItemStackBuilder.of(Material.WARPED_SIGN)
                .name(Component.translatable("pylon.pyloncore.guide.button.search-fluids"))
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            // TODO fluid search page
        }
    }

    fun searchResearches() = object : AbstractItem() {

        override fun getItemProvider(): ItemProvider {
            return ItemStackBuilder.of(Material.CRIMSON_SIGN)
                .name(Component.translatable("pylon.pyloncore.guide.button.search-researches"))
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            // TODO research search page
        }
    }
}