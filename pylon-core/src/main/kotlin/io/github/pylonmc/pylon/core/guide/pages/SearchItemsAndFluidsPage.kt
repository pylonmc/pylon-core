package io.github.pylonmc.pylon.core.guide.pages

import io.github.pylonmc.pylon.core.guide.PylonGuide
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
    }.map {
        val translator = AddonTranslator.translators[getAddon(it)]!!
        val name = translator.translate(
            Component.translatable("pylon.${it.namespace}.item.${it.key}.name"), player.locale()
        )
        check(name != null)
        val plainTextName = serializer.serialize(name).lowercase()
        Pair(ItemButton(it), plainTextName)
    }.toMutableList()

    fun getFluidButtons(player: Player): MutableList<Pair<Item, String>> = PylonRegistry.FLUIDS.getKeys().filter {
        !PylonGuide.hiddenFluids.contains(it)
    }.map {
        val translator = AddonTranslator.translators[getAddon(it)]!!
        val name = translator.translate(
            Component.translatable("pylon.${it.namespace}.fluid.${it.key}"), player.locale()
        )
        check(name != null)
        val plainTextName = serializer.serialize(name).lowercase()
        Pair(FluidButton(it), plainTextName)
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