package io.github.pylonmc.pylon.core.guide.pages.base

import io.github.pylonmc.pylon.core.guide.PylonGuide
import io.github.pylonmc.pylon.core.guide.button.PageButton
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.util.gui.GuiItems
import net.kyori.adventure.text.Component
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
        .name(Component.translatable("pylon.${key.namespace}.guide.page.${key.key}.name"))
        .lore(Component.translatable("pylon.${key.namespace}.guide.page.${key.key}.lore"))

    open fun getHeader(player: Player) = PagedGui.guis()
        .setStructure(
            "p b # # # # # s n",
            "x x x x x x x x x",
            "x x x x x x x x x",
            "x x x x x x x x x",
            "x x x x x x x x x",
            "x x x x x x x x x",
        )
        .addIngredient('#', GuiItems.blankGrayPane())
        .addIngredient('p', GuiItems.pagePrevious())
        .addIngredient('b', PageButton(PylonGuide.rootPage)) // TODO make this actually keep track of history
        .addIngredient('s', PageButton(PylonGuide.searchItemsPage))
        .addIngredient('n', GuiItems.pageNext())
        .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)

    override fun open(player: Player) {
        val gui = getHeader(player)

        val pageSupplier = {
            Gui.normal()
                .setStructure(
                    ". . . . . . . . .",
                    ". . . . . . . . .",
                    ". . . . . . . . .",
                    ". . . . . . . . .",
                    ". . . . . . . . .",
                )
                .build()
        }

        var page = pageSupplier.invoke()
        var j = 0
        for (button in buttonSupplier.get()) {
            if (j >= 45) {
                page = pageSupplier.invoke()
                gui.addContent(page)
            }

            page.setItem(j, button)
            j++
        }

        gui.addContent(page)

        Window.single()
            .setGui(gui)
            .setTitle(AdventureComponentWrapper(Component.translatable("pylon.${key.namespace}.guide.page.${key.key}.title")))
            .open(player)
    }
}