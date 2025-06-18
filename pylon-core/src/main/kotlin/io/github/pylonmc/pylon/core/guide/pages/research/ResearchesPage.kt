package io.github.pylonmc.pylon.core.guide.pages.research

import io.github.pylonmc.pylon.core.guide.PylonGuide
import io.github.pylonmc.pylon.core.guide.button.PageButton
import io.github.pylonmc.pylon.core.guide.pages.base.SimpleDynamicGuidePage
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.Material

class ResearchesPage : SimpleDynamicGuidePage(
    pylonKey("researches"),
    Material.BREWING_STAND,
    {
        PylonRegistry.ADDONS.getValues().filter { addon ->
            PylonRegistry.RESEARCHES.getKeys().any {
                it.namespace == addon.key.namespace && it !in PylonGuide.hiddenResearches
            }
        }.map { addon ->
            PageButton(AddonResearchesPage(addon))
        }
    }
)