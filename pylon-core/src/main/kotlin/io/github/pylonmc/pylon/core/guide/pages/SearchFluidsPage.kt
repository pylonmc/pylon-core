package io.github.pylonmc.pylon.core.guide.pages

import io.github.pylonmc.pylon.core.guide.button.FluidButton
import io.github.pylonmc.pylon.core.guide.pages.base.SearchPage
import io.github.pylonmc.pylon.core.i18n.AddonTranslator
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.key.getAddon
import io.github.pylonmc.pylon.core.util.pylonKey
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player

class SearchFluidsPage : SearchPage(
    pylonKey("search_fluids"),
    Material.WARPED_SIGN
) {
    override fun getItemNamePairs(player: Player, search: String) = PylonRegistry.Companion.FLUIDS.getKeys().map {
        val translator = AddonTranslator.Companion.translators[getAddon(it)]!!
        val name = translator.translate(
            Component.translatable("pylon.${it.namespace}.fluid.${it.key}"), player.locale()
        )
        check(name != null)
        val plainTextName = serializer.serialize(name).lowercase()
        Pair(FluidButton(it), plainTextName)
    }
}