package io.github.pylonmc.pylon.core.guide.pages.base

import io.github.pylonmc.pylon.core.guide.PylonGuide
import io.github.pylonmc.pylon.core.guide.button.BackButton
import io.github.pylonmc.pylon.core.guide.button.PageButton
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.util.gui.GuiItems
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.window.Window
import java.util.function.Supplier

open class SimpleDynamicGuidePage(
    private val key: NamespacedKey,
    val material: Material,
    val buttonSupplier: Supplier<List<Item>>,
) : GuidePage {

    override fun getKey() = key

    override val item = ItemStackBuilder.of(material)
        .name(Component.translatable("pylon.${key.namespace}.guide.page.${key.key}"))

    open fun getHeader(player: Player, buttons: List<Item>) = PagedGui.items()
        .setStructure(
            "< b # # # # # s >",
            "x x x x x x x x x",
            "x x x x x x x x x",
            "x x x x x x x x x",
            "x x x x x x x x x",
            "x x x x x x x x x",
        )
        .addIngredient('#', GuiItems.background())
        .addIngredient('<', if (buttons.size >= 45) GuiItems.pagePrevious() else GuiItems.background())
        .addIngredient('b', BackButton(player))
        .addIngredient('s', PageButton(PylonGuide.searchItemsPage))
        .addIngredient('>', if (buttons.size >= 45) GuiItems.pageNext() else GuiItems.background())
        .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)

    override fun getGui(player: Player): Gui {
        val buttons = buttonSupplier.get()
        val gui = getHeader(player, buttons)

        for (button in buttons) {
            gui.addContent(button)
        }

        return gui.build()
    }
}