package io.github.pylonmc.pylon.core.guide.button

import io.github.pylonmc.pylon.core.guide.pages.fluid.FluidRecipesPage
import io.github.pylonmc.pylon.core.guide.pages.fluid.FluidUsagesPage
import io.github.pylonmc.pylon.core.i18n.PylonArgument
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.gui.unit.UnitFormat
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.item.impl.AbstractItem

open class FluidButton(val key: NamespacedKey, val amount: Double?, val preDisplayDecorator: (ItemStackBuilder) -> ItemStackBuilder) : AbstractItem() {
    constructor(key: NamespacedKey, amount: Double?) : this(key, amount, { it })
    constructor(key: NamespacedKey) : this(key, null)

    val fluid = PylonRegistry.FLUIDS[key] ?: throw IllegalArgumentException("There is no fluid with key $key")

    override fun getItemProvider() = try {
        if (amount == null) {
            preDisplayDecorator.invoke(fluid.getItem())
        } else {
            preDisplayDecorator.invoke(fluid.getItem())
                .name(Component.translatable(
                    "pylon.pyloncore.guide.button.fluid.name",
                    PylonArgument.of("fluid", fluid.getItem().stack.getData(DataComponentTypes.ITEM_NAME)!!),
                    PylonArgument.of("amount", UnitFormat.MILLIBUCKETS.format(amount).decimalPlaces(2))
                ))
        }
    } catch (e: Exception) {
        e.printStackTrace()
        ItemStackBuilder.of(Material.BARRIER)
            .name(Component.translatable("pylon.pyloncore.guide.button.fluid.error"))
    }

    override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
        try {
            if (clickType.isLeftClick) {
                val page = FluidRecipesPage(fluid.key)
                if (page.pages.isNotEmpty()) {
                    page.open(player)
                }
            } else {
                val page = FluidUsagesPage(fluid.key)
                if (page.pages.isNotEmpty()) {
                    page.open(player)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
