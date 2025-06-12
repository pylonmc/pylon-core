package io.github.pylonmc.pylon.core.guide.button

import io.github.pylonmc.pylon.core.registry.PylonRegistry
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import xyz.xenondevs.invui.item.impl.AbstractItem

open class FluidButton(val key: NamespacedKey) : AbstractItem() {

    val fluid = PylonRegistry.FLUIDS[key] ?: throw IllegalArgumentException("There is no fluid with key $key")

    override fun getItemProvider() = fluid.getItem()

    override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
        if (clickType.isRightClick) {
            // TODO open usages page
        }
    }
}
