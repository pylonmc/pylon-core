package io.github.pylonmc.pylon.core.guide.pages.research

import io.github.pylonmc.pylon.core.addon.PylonAddon
import io.github.pylonmc.pylon.core.content.guide.PylonGuide
import io.github.pylonmc.pylon.core.guide.button.ResearchButton
import io.github.pylonmc.pylon.core.guide.pages.base.SimpleDynamicGuidePage
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.pylonKey
import net.kyori.adventure.text.Component

/**
 * Shows all the researches for the given [addon].
 */
class AddonResearchesPage(val addon: PylonAddon) : SimpleDynamicGuidePage(
    pylonKey("researches_" + addon.key.key),
    addon.material,
    {
        PylonRegistry.RESEARCHES.filter {
            it.key.namespace == addon.key.namespace && it.key !in PylonGuide.hiddenResearches
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