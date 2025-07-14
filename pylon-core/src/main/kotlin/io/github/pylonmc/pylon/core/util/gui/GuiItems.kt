@file:Suppress("UnstableApiUsage")

package io.github.pylonmc.pylon.core.util.gui

import io.github.pylonmc.pylon.core.i18n.PylonArgument
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.papermc.paper.datacomponent.DataComponentTypes
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

object GuiItems {
    @JvmStatic
    fun background(): Item = SimpleItem(
        ItemStackBuilder.of(Material.GRAY_STAINED_GLASS_PANE)
            .name("")
            .set(DataComponentTypes.HIDE_TOOLTIP)
    )

    @JvmStatic
    fun backgroundBlack(): Item = SimpleItem(
        ItemStackBuilder.of(Material.BLACK_STAINED_GLASS_PANE)
            .name("")
            .set(DataComponentTypes.HIDE_TOOLTIP)
    )

    /**
     * Item that cycles through durability to represent processing time. Intended for use in
     * recipe displays. For a proper progress bar item, see [ProgressItem]
     */
    @JvmStatic
    fun progressCyclingItem(timeTicks: Int, template: ItemStackBuilder): Item {
        val states: MutableList<ItemStackBuilder> = mutableListOf()
        var i = 0
        while (i < timeTicks) {
            states.add(ItemStackBuilder.of(template.build().clone())
                .set(DataComponentTypes.MAX_DAMAGE, timeTicks)
                .set(DataComponentTypes.DAMAGE, i)
            )
            i++
        }
        return AutoCycleItem(1, *(states.toTypedArray()))
    }

    @JvmStatic
    fun scrollUp(): Item = PylonScrollItem(-1, "up")

    @JvmStatic
    fun scrollDown(): Item = PylonScrollItem(1, "down")

    @JvmStatic
    fun scrollLeft(): Item = PylonScrollItem(-1, "left")

    @JvmStatic
    fun scrollRight(): Item = PylonScrollItem(1, "right")

    @JvmStatic
    fun pageNext(): Item = PylonPageItem(true)

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
    private val name = Component.translatable("pylon.pyloncore.gui.page.${if (forward) "next" else "previous"}")

    override fun getItemProvider(gui: PagedGui<*>): ItemProvider {
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

