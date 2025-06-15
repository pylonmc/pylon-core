package io.github.pylonmc.pylon.core.guide.pages.item

import io.github.pylonmc.pylon.core.guide.PylonGuide
import io.github.pylonmc.pylon.core.guide.button.ItemButton
import io.github.pylonmc.pylon.core.guide.pages.base.SearchPage
import io.github.pylonmc.pylon.core.i18n.AddonTranslator
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.key.getAddon
import io.github.pylonmc.pylon.core.util.pylonKey
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player

class SearchItemsPage : SearchPage(
    pylonKey("search_items"),
    Material.OAK_SIGN
) {
    override fun getItemNamePairs(player: Player, search: String) = PylonRegistry.Companion.ITEMS.getKeys().filter {
        !PylonGuide.Companion.hiddenItems.contains(it)
    }.map {
        val translator = AddonTranslator.Companion.translators[getAddon(it)]!!
        val name = translator.translate(
            Component.translatable("pylon.${it.namespace}.item.${it.key}.name"), player.locale()
        )
        check(name != null)
        val plainTextName = serializer.serialize(name).lowercase()
        Pair(ItemButton(it), plainTextName)
    }
}