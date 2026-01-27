package io.github.pylonmc.rebar.guide.pages.settings

import io.github.pylonmc.rebar.config.PylonConfig
import io.github.pylonmc.rebar.guide.button.PageButton
import io.github.pylonmc.rebar.guide.button.setting.TogglePlayerSettingButton
import io.github.pylonmc.rebar.resourcepack.armor.ArmorTextureEngine.hasCustomArmorTextures
import io.github.pylonmc.rebar.util.rebarKey
import org.bukkit.Material

object ResourcePackSettingsPage : PlayerSettingsPage(rebarKey("resource_pack_settings")) {

    @JvmStatic
    val blockTextureSettingsButton = PageButton(Material.BOOKSHELF, BlockTextureSettingsPage)

    init {
        if (PylonConfig.ArmorTextureConfig.ENABLED && !PylonConfig.ArmorTextureConfig.FORCED) {
            addSetting(
                TogglePlayerSettingButton(
                    rebarKey("toggle-armor-textures"),
                    toggle = { player -> player.hasCustomArmorTextures = !player.hasCustomArmorTextures },
                    isEnabled = { player -> player.hasCustomArmorTextures },
                )
            )
        }

        if (PylonConfig.BlockTextureConfig.ENABLED) {
            addSetting(blockTextureSettingsButton)
        }
    }
}