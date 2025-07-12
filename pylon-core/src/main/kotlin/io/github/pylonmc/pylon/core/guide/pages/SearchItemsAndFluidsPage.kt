package io.github.pylonmc.pylon.core.guide.pages

import io.github.pylonmc.pylon.core.content.guide.PylonGuide
import io.github.pylonmc.pylon.core.guide.button.FluidButton
import io.github.pylonmc.pylon.core.guide.button.ItemButton
import io.github.pylonmc.pylon.core.guide.pages.base.SearchPage
import io.github.pylonmc.pylon.core.i18n.AddonTranslator
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.key.getAddon
import io.github.pylonmc.pylon.core.util.pylonKey
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Material
import org.bukkit.entity.Player
import xyz.xenondevs.invui.item.Item

class SearchItemsAndFluidsPage : SearchPage(
    pylonKey("search"),
    Material.OAK_SIGN
) {

    fun getItemButtons(player: Player): MutableList<Pair<Item, String>> = PylonRegistry.ITEMS.getKeys().filter {
        !PylonGuide.hiddenItems.contains(it)
    }.mapNotNull { item ->
        val translator = AddonTranslator.translators[getAddon(item)]!!
        val name = translator.translate(
            Component.translatable("pylon.${item.namespace}.item.${item.key}.name"), player.locale()
        )
        name?.let { Pair(ItemButton(item), serializer.serialize(name).lowercase()) }

    }.toMutableList()

    fun getFluidButtons(player: Player): MutableList<Pair<Item, String>> = PylonRegistry.FLUIDS.getKeys().filter {
        !PylonGuide.hiddenFluids.contains(it)
    }.mapNotNull { fluid ->
        val translator = AddonTranslator.translators[getAddon(fluid)]!!
        val name = translator.translate(
            Component.translatable("pylon.${fluid.namespace}.fluid.${fluid.key}"), player.locale()
        )
        name?.let { Pair(FluidButton(fluid), serializer.serialize(name).lowercase()) }
    }.toMutableList()

    override fun getItemNamePairs(player: Player, search: String): List<Pair<Item, String>> {
        val list = getItemButtons(player)
        list.addAll(getFluidButtons(player))
        return list
    }

    companion object {
        private val serializer = PlainTextComponentSerializer.plainText()
    }
}