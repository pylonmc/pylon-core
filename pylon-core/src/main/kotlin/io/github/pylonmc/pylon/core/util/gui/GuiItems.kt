@file:Suppress("UnstableApiUsage")

package io.github.pylonmc.pylon.core.util.gui

import io.github.pylonmc.pylon.core.i18n.PylonArgument
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.util.gui.GuiItems.background
import io.github.pylonmc.pylon.core.util.pylonKey
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.TooltipDisplay
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import xyz.xenondevs.invui.Click
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.ScrollGui
import xyz.xenondevs.invui.item.AbstractPagedGuiBoundItem
import xyz.xenondevs.invui.item.AbstractScrollGuiBoundItem
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemProvider

/**
 * A utility class containing items commonly used in GUIs.
 */
object GuiItems {
    val pylonGuiItemKeyKey = pylonKey("gui_item_key")

    /**
     * A gray glass pane with no name or lore.
     */
    @JvmStatic
    @JvmOverloads
    fun background(name: String = ""): Item = Item.simple(
        ItemStackBuilder.gui(Material.GRAY_STAINED_GLASS_PANE, pylonKey("background"))
            .name(name)
            .set(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().hideTooltip(true))
    )

    /**
     * A black glass pane with no name or lore.
     */
    @JvmStatic
    @JvmOverloads
    fun backgroundBlack(name: String = ""): Item = Item.simple(
        ItemStackBuilder.gui(Material.BLACK_STAINED_GLASS_PANE, pylonKey("background_black"))
            .name(name)
            .set(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().hideTooltip(true))
    )

    /**
     * A lime glass pane named 'Input'
     */
    @JvmStatic
    fun input(): Item = Item.simple(
        ItemStackBuilder.gui(Material.LIME_STAINED_GLASS_PANE, pylonKey("input"))
            .name(Component.translatable("pylon.pyloncore.gui.input"))
    )

    /**
     * An orange glass pane named 'Output'
     */
    @JvmStatic
    fun output(): Item = Item.simple(
        ItemStackBuilder.gui(Material.ORANGE_STAINED_GLASS_PANE, pylonKey("output"))
            .name(Component.translatable("pylon.pyloncore.gui.output"))
    )

    /**
     * Item that automatically cycles through durability to represent processing time.
     * Intended for use in recipe displays.
     *
     * For example, you could create a progressCyclingItem
     * to represent a grindstone's grinding time in the guide. In this case, you might want to
     * set [template] to the grindstone item, and [timeTicks] to the grinding time.
     *
     * For a more flexible progress bar item which does not automatically cycle, see [ProgressItem]
     */
    @JvmStatic
    fun progressCyclingItem(timeTicks: Int, template: ItemStackBuilder): Item {
        val states: MutableList<ItemStackBuilder> = mutableListOf()
        var i = 0
        while (i < timeTicks) {
            states.add(
                ItemStackBuilder.of(template.build().clone())
                    .set(DataComponentTypes.MAX_DAMAGE, timeTicks)
                    .set(DataComponentTypes.DAMAGE, i)
                    .set(
                        DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay()
                            .addHiddenComponents(DataComponentTypes.DAMAGE, DataComponentTypes.MAX_DAMAGE)
                    )
            )
            i++
        }
        return Item.builder()
            .setCyclingItemProvider(1, states)
            .build()
    }

    /**
     * A button that scrolls one GUI row up. This should only be used in a [ScrollGui].
     */
    @JvmStatic
    fun scrollUp(): Item = PylonScrollItem(-1, "up")

    /**
     * A button that scrolls one GUI row down. This should only be used in a [ScrollGui].
     */
    @JvmStatic
    fun scrollDown(): Item = PylonScrollItem(1, "down")

    /**
     * A button that scrolls one GUI row left. This should only be used in a [ScrollGui].
     */
    @JvmStatic
    fun scrollLeft(): Item = PylonScrollItem(-1, "left")

    /**
     * A button that scrolls one GUI row right. This should only be used in a [ScrollGui].
     */
    @JvmStatic
    fun scrollRight(): Item = PylonScrollItem(1, "right")

    /**
     * A button that goes to the next page. This should only be used in a [PagedGui]
     */
    @JvmStatic
    fun pageNext(): Item = PylonPageItem(true)

    /**
     * A button that goes to the previous page. This should only be used in a [PagedGui]
     */
    @JvmStatic
    fun pagePrevious(): Item = PylonPageItem(false)
}

private class PylonScrollItem(private val direction: Int, private val key: String) : AbstractScrollGuiBoundItem() {
    private val name = Component.translatable("pylon.pyloncore.gui.scroll.$key")

    override fun getItemProvider(viewer: Player): ItemProvider {
        val material = if (gui.canScroll) Material.GREEN_STAINED_GLASS_PANE else Material.RED_STAINED_GLASS_PANE
        return ItemStackBuilder.gui(material, pylonKey("scroll_$key")).name(name)
    }

    override fun handleClick(clickType: ClickType, player: Player, click: Click) {
        gui.line += direction
    }

    private val ScrollGui<*>.canScroll: Boolean
        get() = if (direction > 0) line < maxLine else line > 0
}

private class PylonPageItem(private val forward: Boolean) : AbstractPagedGuiBoundItem() {
    private val background = background()
    private val name = Component.translatable("pylon.pyloncore.gui.page.${if (forward) "next" else "previous"}")

    override fun getItemProvider(viewer: Player): ItemProvider {
        if (gui.pageCount < 2) return background.getItemProvider(viewer)

        val material = if (gui.canPage) Material.GREEN_STAINED_GLASS_PANE else Material.RED_STAINED_GLASS_PANE
        return ItemStackBuilder.gui(material, pylonKey("page_${if (forward) "next" else "previous"}"))
            .name(
                name.arguments(
                    PylonArgument.of("current", gui.page + 1),
                    PylonArgument.of("total", gui.page),
                )
            )
    }

    override fun handleClick(clickType: ClickType, player: Player, click: Click) {
        if (forward) gui.page++ else gui.page--
    }

    private val PagedGui<*>.canPage: Boolean
        get() = if (forward) page < pageCount - 1 else page > 0
}

