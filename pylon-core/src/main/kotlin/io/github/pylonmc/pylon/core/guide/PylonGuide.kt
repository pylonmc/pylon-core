package io.github.pylonmc.pylon.core.guide

import io.github.pylonmc.pylon.core.guide.views.MainView
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
            MainView.open(event.player)
        }
    }

    companion object {

        val KEY = pylonKey("guide")
        val STACK = ItemStackBuilder.pylonItem(Material.ENCHANTED_BOOK, KEY)
            .build()
    }
}