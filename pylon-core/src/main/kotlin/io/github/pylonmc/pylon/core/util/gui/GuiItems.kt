@file:Suppress("UnstableApiUsage")

package io.github.pylonmc.pylon.core.util.gui

import io.github.pylonmc.pylon.core.i18n.PylonArgument
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.util.gui.GuiItems.background
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.TooltipDisplay
import net.kyori.adventure.text.Component
import org.bukkit.Material
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.ScrollGui
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AutoCycleItem
import xyz.xenondevs.invui.item.impl.SimpleItem
import xyz.xenondevs.invui.item.impl.controlitem.PageItem
import xyz.xenondevs.invui.item.impl.controlitem.ScrollItem

/**
 * A utility class containing items commonly used in GUIs.
 */
object GuiItems {
    /**
     * A gray glass pane with no name or lore.
     */
    @JvmStatic
    @JvmOverloads
    fun background(name: String = ""): Item = SimpleItem(
        ItemStackBuilder.of(Material.GRAY_STAINED_GLASS_PANE)
            .name(name)
            .set(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().hideTooltip(true))
    )

    /**
     * A black glass pane with no name or lore.
     */
    @JvmStatic
    @JvmOverloads
    fun backgroundBlack(name: String = ""): Item = SimpleItem(
        ItemStackBuilder.of(Material.BLACK_STAINED_GLASS_PANE)
            .name(name)
            .set(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().hideTooltip(true))
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

private class PylonScrollItem(private val direction: Int, key: String?) : ScrollItem(direction) {
    private val name = Component.translatable("pylon.pyloncore.gui.scroll.$key")

    override fun getItemProvider(gui: ScrollGui<*>): ItemProvider {
        val material =
            if (gui.canScroll(direction)) Material.GREEN_STAINED_GLASS_PANE else Material.RED_STAINED_GLASS_PANE
        return ItemStackBuilder.of(material).name(name)
    }
}

private class PylonPageItem(private val forward: Boolean) : PageItem(forward) {
    private val background = background().itemProvider
    private val name = Component.translatable("pylon.pyloncore.gui.page.${if (forward) "next" else "previous"}")

    override fun getItemProvider(gui: PagedGui<*>): ItemProvider {
        if (gui.pageAmount < 2) return background

        val material = if (gui.canPage) Material.GREEN_STAINED_GLASS_PANE else Material.RED_STAINED_GLASS_PANE
        return ItemStackBuilder.of(material)
            .name(
                name.arguments(
                    PylonArgument.of("current", gui.currentPage + 1),
                    PylonArgument.of("total", gui.pageAmount),
                )
            )
    }

    private val PagedGui<*>.canPage: Boolean
        get() = if (forward) hasNextPage() else hasPreviousPage()
}

