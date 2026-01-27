@file:Suppress("UnstableApiUsage")

package io.github.pylonmc.rebar.util.gui

import io.github.pylonmc.rebar.i18n.RebarArgument
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder
import io.github.pylonmc.rebar.util.gui.GuiItems.background
import io.github.pylonmc.rebar.util.rebarKey
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.TooltipDisplay
import net.kyori.adventure.text.Component
import org.bukkit.Material
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.ScrollGui
import xyz.xenondevs.invui.gui.TabGui
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AutoCycleItem
import xyz.xenondevs.invui.item.impl.SimpleItem
import xyz.xenondevs.invui.item.impl.controlitem.PageItem
import xyz.xenondevs.invui.item.impl.controlitem.ScrollItem
import xyz.xenondevs.invui.item.impl.controlitem.TabItem

/**
 * A utility class containing items commonly used in GUIs.
 */
object GuiItems {
    val rebarGuiItemKeyKey = rebarKey("gui_item_key")

    /**
     * A gray glass pane with no name or lore.
     */
    @JvmStatic
    @JvmOverloads
    fun background(name: String = ""): Item = SimpleItem(
        ItemStackBuilder.gui(Material.GRAY_STAINED_GLASS_PANE, rebarKey("background"))
            .name(name)
            .set(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().hideTooltip(true))
    )

    /**
     * A black glass pane with no name or lore.
     */
    @JvmStatic
    @JvmOverloads
    fun backgroundBlack(name: String = ""): Item = SimpleItem(
        ItemStackBuilder.gui(Material.BLACK_STAINED_GLASS_PANE, rebarKey("background_black"))
            .name(name)
            .set(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().hideTooltip(true))
    )

    /**
     * A lime glass pane named 'Input'
     */
    @JvmStatic
    fun input(): Item = SimpleItem(
        ItemStackBuilder.gui(Material.LIME_STAINED_GLASS_PANE, rebarKey("input"))
            .name(Component.translatable("rebar.gui.input"))
    )

    /**
     * An orange glass pane named 'Output'
     */
    @JvmStatic
    fun output(): Item = SimpleItem(
        ItemStackBuilder.gui(Material.ORANGE_STAINED_GLASS_PANE, rebarKey("output"))
            .name(Component.translatable("rebar.gui.output"))
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
            states.add(ItemStackBuilder.of(template.build().clone())
                .set(DataComponentTypes.MAX_DAMAGE, timeTicks)
                .set(DataComponentTypes.DAMAGE, i)
                .set(
                    DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay()
                        .addHiddenComponents(DataComponentTypes.DAMAGE, DataComponentTypes.MAX_DAMAGE)
                )
            )
            i++
        }
        return AutoCycleItem(1, *(states.toTypedArray()))
    }

    /**
     * A button that scrolls one GUI row up. This should only be used in a [ScrollGui].
     */
    @JvmStatic
    fun scrollUp(): Item = RebarScrollItem(-1, "up")

    /**
     * A button that scrolls one GUI row down. This should only be used in a [ScrollGui].
     */
    @JvmStatic
    fun scrollDown(): Item = RebarScrollItem(1, "down")

    /**
     * A button that scrolls one GUI row left. This should only be used in a [ScrollGui].
     */
    @JvmStatic
    fun scrollLeft(): Item = RebarScrollItem(-1, "left")

    /**
     * A button that scrolls one GUI row right. This should only be used in a [ScrollGui].
     */
    @JvmStatic
    fun scrollRight(): Item = RebarScrollItem(1, "right")

    /**
     * A button that goes to the next page. This should only be used in a [PagedGui]
     */
    @JvmStatic
    fun pageNext(): Item = RebarPageItem(true)

    /**
     * A button that goes to the previous page. This should only be used in a [PagedGui]
     */
    @JvmStatic
    fun pagePrevious(): Item = RebarPageItem(false)

    /**
     * A button that goes to the specified tab. This should only be used in a [TabGui]
     */
    @JvmStatic
    fun tab(item: ItemStackBuilder, tab: Int): Item = RebarTabItem(item, tab)
}

private class RebarScrollItem(private val direction: Int, private val key: String?) : ScrollItem(direction) {
    private val name = Component.translatable("rebar.gui.scroll.$key")

    override fun getItemProvider(gui: ScrollGui<*>): ItemProvider {
        val material =
            if (gui.canScroll(direction)) Material.GREEN_STAINED_GLASS_PANE else Material.RED_STAINED_GLASS_PANE
        return ItemStackBuilder.gui(material, rebarKey("scroll_$key")).name(name)
    }
}

private class RebarPageItem(private val forward: Boolean) : PageItem(forward) {
    private val background = background().itemProvider
    private val name = Component.translatable("rebar.gui.page.${if (forward) "next" else "previous"}")

    override fun getItemProvider(gui: PagedGui<*>): ItemProvider {
        if (gui.pageAmount < 2) return background

        val material = if (gui.canPage) Material.GREEN_STAINED_GLASS_PANE else Material.RED_STAINED_GLASS_PANE
        return ItemStackBuilder.gui(material, rebarKey("page_${if (forward) "next" else "previous"}"))
            .name(
                name.arguments(
                    RebarArgument.of("current", gui.currentPage + 1),
                    RebarArgument.of("total", gui.pageAmount),
                )
            )
    }

    private val PagedGui<*>.canPage: Boolean
        get() = if (forward) hasNextPage() else hasPreviousPage()
}

private class RebarTabItem(private val item: ItemStackBuilder, private val tab: Int) : TabItem(tab) {
    override fun getItemProvider(gui: TabGui): ItemProvider {
        return if (gui.currentTab == tab) {
            item.clone().set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
        } else {
            item
        }
    }
}

