package io.github.pylonmc.pylon.core.guide.pages.research

import io.github.pylonmc.pylon.core.content.guide.PylonGuide
import io.github.pylonmc.pylon.core.guide.pages.base.SimpleDynamicGuidePage
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.pylonKey

/**
 * Shows buttons to view each addon's researches.
 */
class ResearchesPage : SimpleDynamicGuidePage(
    pylonKey("researches"),
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