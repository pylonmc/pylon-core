package io.github.pylonmc.pylon.core.guide.pages.settings

import io.github.pylonmc.pylon.core.config.PylonConfig
import io.github.pylonmc.pylon.core.guide.button.setting.CyclePlayerSettingButton
import io.github.pylonmc.pylon.core.guide.button.setting.TogglePlayerSettingButton
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.util.pylonKey
import io.github.pylonmc.pylon.core.waila.Waila.Companion.wailaConfig
import org.bukkit.Material

object WailaSettingsPage : PlayerSettingsPage(pylonKey("waila_settings")) {
    init {
        addSetting(
            TogglePlayerSettingButton(
                pylonKey("toggle-waila"),
                toggle = { player -> player.wailaConfig.enabled = !player.wailaConfig.enabled },
                isEnabled = { player -> player.wailaConfig.enabled }
            ))
        addSetting(
            TogglePlayerSettingButton(
                pylonKey("toggle-vanilla-waila"),
                toggle = { player ->
                    player.wailaConfig.vanillaWailaEnabled = !player.wailaConfig.vanillaWailaEnabled
                },
                isEnabled = { player -> player.wailaConfig.vanillaWailaEnabled }
            ))
        if (PylonConfig.WailaConfig.enabledTypes.size > 1) {
            addSetting(
                CyclePlayerSettingButton(
                    pylonKey("cycle-waila-type"),
                    PylonConfig.WailaConfig.enabledTypes,
                    identifier = { type -> type.name.lowercase() },
                    getter = { player -> player.wailaConfig.type },
                    setter = { player, type -> player.wailaConfig.type = type },
                    decorator = { player, type ->
                        ItemStackBuilder.Companion.of(Material.PAPER)
                            .addCustomModelDataString("waila_type=${type.name.lowercase()}")
                            .build()
                    }
                ))
        }
    }
}