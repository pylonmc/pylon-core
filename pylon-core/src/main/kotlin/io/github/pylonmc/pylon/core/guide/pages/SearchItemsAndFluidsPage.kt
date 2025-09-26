package io.github.pylonmc.pylon.core.guide.pages

import io.github.pylonmc.pylon.core.content.guide.PylonGuide
import io.github.pylonmc.pylon.core.guide.button.FluidButton
import io.github.pylonmc.pylon.core.guide.button.ItemButton
import io.github.pylonmc.pylon.core.guide.pages.base.SearchPage
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.plainText
import io.github.pylonmc.pylon.core.util.pylonKey
import io.github.pylonmc.pylon.core.util.globalRender
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import xyz.xenondevs.invui.item.Item

class SearchItemsAndFluidsPage : SearchPage(
    pylonKey("search"),
    Material.OAK_SIGN
) {

    fun getItemButtons(player: Player): MutableList<Pair<Item, String>> = PylonRegistry.ITEMS.filter {
        it.key !in PylonGuide.hiddenItems
    }.map { item ->
        val name = Component.translatable("pylon.${item.key.namespace}.item.${item.key.key}.name").globalRender(player.locale())
        ItemButton(item.itemStack) to name.plainText.lowercase()
    }.toMutableList()

    fun getFluidButtons(player: Player): MutableList<Pair<Item, String>> = PylonRegistry.FLUIDS.filter {
        it.key !in PylonGuide.hiddenFluids
    }.map { fluid ->
        val name = Component.translatable("pylon.${fluid.key.namespace}.fluid.${fluid.key.key}").globalRender(player.locale())
        FluidButton(fluid) to name.plainText.lowercase()
    }.toMutableList()

    override fun getItemNamePairs(player: Player, search: String): List<Pair<Item, String>> {
        val list = getItemButtons(player)
        list.addAll(getFluidButtons(player))
        return list
    }
}