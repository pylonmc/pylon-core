package io.github.pylonmc.rebar.guide.pages.research

import io.github.pylonmc.rebar.addon.PylonAddon
import io.github.pylonmc.rebar.content.guide.PylonGuide
import io.github.pylonmc.rebar.guide.button.ResearchButton
import io.github.pylonmc.rebar.guide.pages.base.SimpleDynamicGuidePage
import io.github.pylonmc.rebar.registry.PylonRegistry
import io.github.pylonmc.rebar.util.rebarKey
import net.kyori.adventure.text.format.Style

/**
 * Shows all the researches for the given [addon].
 */
class AddonResearchesPage(val addon: PylonAddon) : SimpleDynamicGuidePage(
    rebarKey("researches_" + addon.key.key),
    {
        PylonRegistry.RESEARCHES.filter {
            it.key.namespace == addon.key.namespace && it.key !in PylonGuide.hiddenResearches
        }.map {
            ResearchButton(it)
        }
    }
) {
    override val title = addon.displayName.style(Style.empty())
}