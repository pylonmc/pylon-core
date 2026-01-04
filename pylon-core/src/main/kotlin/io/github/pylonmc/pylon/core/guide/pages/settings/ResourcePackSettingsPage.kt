package io.github.pylonmc.pylon.core.guide.pages.settings

import io.github.pylonmc.pylon.core.config.PylonConfig
import io.github.pylonmc.pylon.core.guide.button.PageButton
import io.github.pylonmc.pylon.core.guide.button.setting.TogglePlayerSettingButton
import io.github.pylonmc.pylon.core.resourcepack.armor.ArmorTextureEngine.hasCustomArmorTextures
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.Material

object ResourcePackSettingsPage : PlayerSettingsPage(pylonKey("resource_pack_settings")) {

    @JvmStatic
    val blockTextureSettingsButton = PageButton(Material.BOOKSHELF, BlockTextureSettingsPage)

    init {
        if (PylonConfig.ArmorTextureConfig.enabled && !PylonConfig.ArmorTextureConfig.forced) {
            addSetting(
                TogglePlayerSettingButton(
                    pylonKey("toggle-armor-textures"),
                    toggle = { player -> player.hasCustomArmorTextures = !player.hasCustomArmorTextures },
                    isEnabled = { player -> player.hasCustomArmorTextures },
                )
            )
        }

        if (PylonConfig.BlockTextureConfig.enabled) {
            addSetting(blockTextureSettingsButton)
        }
    }
}