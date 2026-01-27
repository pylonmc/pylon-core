package io.github.pylonmc.rebar.guide.pages.research

import io.github.pylonmc.rebar.content.guide.RebarGuide
import io.github.pylonmc.rebar.guide.pages.base.SimpleDynamicGuidePage
import io.github.pylonmc.rebar.registry.RebarRegistry
import io.github.pylonmc.rebar.util.rebarKey

/**
 * Shows buttons to view each addon's researches.
 */
class ResearchesPage : SimpleDynamicGuidePage(
    rebarKey("researches"),
    {
        RebarRegistry.ADDONS.getValues().filter { addon ->
            RebarRegistry.RESEARCHES.getKeys().any {
                it.namespace == addon.key.namespace && it !in RebarGuide.hiddenResearches
            }
        }.map { addon ->
            RebarGuide.addonResearchesButton(addon)
        }
    }
)