package io.github.pylonmc.pylon.core.guide.pages.settings

import io.github.pylonmc.pylon.core.config.PylonConfig
import io.github.pylonmc.pylon.core.guide.button.PageButton
import io.github.pylonmc.pylon.core.guide.button.setting.TogglePlayerSettingButton
import io.github.pylonmc.pylon.core.item.research.Research.Companion.guideHints
import io.github.pylonmc.pylon.core.item.research.Research.Companion.researchConfetti
import io.github.pylonmc.pylon.core.item.research.Research.Companion.researchSounds
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.Material

object MainSettingsPage : PlayerSettingsPage(pylonKey("settings")) {

    @JvmStatic
    val wailaSettingsButton = PageButton(Material.SPYGLASS, WailaSettingsPage)

    @JvmStatic
    val resourcePackSettingsButton = PageButton(Material.PAINTING, ResourcePackSettingsPage)

    @JvmStatic
    val researchConfettiButton = TogglePlayerSettingButton(
        pylonKey("toggle-research-confetti"),
        toggle = { player -> player.researchConfetti = !player.researchConfetti },
        isEnabled = { player -> player.researchConfetti }
    )

    @JvmStatic
    val researchSoundsButton = TogglePlayerSettingButton(
        pylonKey("toggle-research-sounds"),
        toggle = { player -> player.researchSounds = !player.researchSounds },
        isEnabled = { player -> player.researchSounds }
    )

    @JvmStatic
    val guideHintsButton = TogglePlayerSettingButton(
        pylonKey("toggle-guide-hints"),
        toggle = { player -> player.guideHints = !player.guideHints },
        isEnabled = { player -> player.guideHints }
    )

    init {
        if (PylonConfig.WailaConfig.enabled) {
            addSetting(wailaSettingsButton)
        }

        addSetting(resourcePackSettingsButton)

        if (PylonConfig.researchesEnabled) {
            addSetting(researchConfettiButton)
            addSetting(researchSoundsButton)
        }

        addSetting(guideHintsButton)
    }
}