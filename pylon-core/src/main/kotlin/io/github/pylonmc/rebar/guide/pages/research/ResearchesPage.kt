package io.github.pylonmc.rebar.guide.pages.research

import io.github.pylonmc.rebar.content.guide.PylonGuide
import io.github.pylonmc.rebar.guide.pages.base.SimpleDynamicGuidePage
import io.github.pylonmc.rebar.registry.PylonRegistry
import io.github.pylonmc.rebar.util.rebarKey

/**
 * Shows buttons to view each addon's researches.
 */
class ResearchesPage : SimpleDynamicGuidePage(
    rebarKey("researches"),
    {
        PylonRegistry.ADDONS.getValues().filter { addon ->
            PylonRegistry.RESEARCHES.getKeys().any {
                it.namespace == addon.key.namespace && it !in PylonGuide.hiddenResearches
            }
        }.map { addon ->
            PylonGuide.addonResearchesButton(addon)
        }
    }
)