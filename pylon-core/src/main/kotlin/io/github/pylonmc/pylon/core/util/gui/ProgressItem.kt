package io.github.pylonmc.pylon.core.util.gui

import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AbstractItem

abstract class ProgressItem @JvmOverloads constructor(
    private val material: Material,
    private val inverse: Boolean = false
) : AbstractItem() {

    var progress: Double = 0.0
        set(value) {
            field = value.coerceIn(0.0, 1.0)
            notifyWindows()
        }

    @Suppress("UnstableApiUsage")
    override fun getItemProvider(): ItemProvider {
        var progressValue = progress
        if (!inverse) {
            progressValue = 1 - progressValue
        }
        val builder = ItemStackBuilder.of(material)
            .set(DataComponentTypes.DAMAGE, (progressValue * material.maxDurability).toInt())
        completeItem(builder)
        return builder
    }

    protected abstract fun completeItem(builder: ItemStackBuilder)

    override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {}
}