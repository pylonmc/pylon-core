package io.github.pylonmc.pylon.core.util.gui

import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.text.Component
import org.bukkit.Material
import xyz.xenondevs.invui.gui.ScrollGui
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.SimpleItem
import xyz.xenondevs.invui.item.impl.controlitem.ScrollItem

@Suppress("UnstableApiUsage")
object GuiItems {
    @JvmStatic
    fun background(): Item = SimpleItem(
        ItemStackBuilder.of(Material.GRAY_STAINED_GLASS_PANE)
            .name("")
            .set(DataComponentTypes.HIDE_TOOLTIP)
    )

    @JvmStatic
    fun scrollUp(): Item = PylonScrollItem(-1, "up")
    @JvmStatic
    fun scrollDown(): Item = PylonScrollItem(1, "down")
    @JvmStatic
    fun scrollLeft(): Item = PylonScrollItem(-1, "left")
    @JvmStatic
    fun scrollRight(): Item = PylonScrollItem(1, "right")
}

private class PylonScrollItem(private val direction: Int, key: String?) : ScrollItem(direction) {
    private val name = Component.translatable("pylon.pyloncore.gui.scroll.$key")

    override fun getItemProvider(gui: ScrollGui<*>): ItemProvider {
        val material = if (gui.canScroll(direction)) Material.GREEN_STAINED_GLASS_PANE else Material.RED_STAINED_GLASS_PANE
        return ItemStackBuilder.of(material).name(name)
    }
}
