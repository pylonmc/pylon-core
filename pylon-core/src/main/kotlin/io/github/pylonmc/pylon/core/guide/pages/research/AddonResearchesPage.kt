package io.github.pylonmc.pylon.core.guide.pages.research

import io.github.pylonmc.pylon.core.addon.PylonAddon
import io.github.pylonmc.pylon.core.guide.PylonGuide
import io.github.pylonmc.pylon.core.guide.button.ResearchButton
import io.github.pylonmc.pylon.core.guide.pages.base.SimpleDynamicGuidePage
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.pylonKey
import net.kyori.adventure.text.Component

class AddonResearchesPage(val addon: PylonAddon) : SimpleDynamicGuidePage(
    pylonKey("researches_" + addon.key.key),
    addon.material,
    {
        PylonRegistry.RESEARCHES.getKeys().filter {
            it.namespace == addon.key.namespace && it !in PylonGuide.hiddenResearches
        }.map {
            ResearchButton(it)
        }
    }
) {
    override val item: ItemStackBuilder
        get() = ItemStackBuilder.of(addon.material)
            .name(addon.displayName)

    override val title: Component
        get() = Component.translatable("pylon.pyloncore.guide.page.researches")
}