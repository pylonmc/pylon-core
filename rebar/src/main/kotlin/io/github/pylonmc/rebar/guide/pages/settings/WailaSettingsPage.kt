package io.github.pylonmc.rebar.guide.pages.settings

import io.github.pylonmc.rebar.config.RebarConfig
import io.github.pylonmc.rebar.guide.button.setting.CyclePlayerSettingButton
import io.github.pylonmc.rebar.guide.button.setting.TogglePlayerSettingButton
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder
import io.github.pylonmc.rebar.util.rebarKey
import io.github.pylonmc.rebar.waila.Waila.Companion.wailaConfig
import org.bukkit.Material

object WailaSettingsPage : PlayerSettingsPage(rebarKey("waila_settings")) {
    init {
        addSetting(
            TogglePlayerSettingButton(
                rebarKey("toggle-waila"),
                toggle = { player -> player.wailaConfig.enabled = !player.wailaConfig.enabled },
                isEnabled = { player -> player.wailaConfig.enabled }
            ))
        addSetting(
            TogglePlayerSettingButton(
                rebarKey("toggle-vanilla-waila"),
                toggle = { player ->
                    player.wailaConfig.vanillaWailaEnabled = !player.wailaConfig.vanillaWailaEnabled
                },
                isEnabled = { player -> player.wailaConfig.vanillaWailaEnabled }
            ))
        if (RebarConfig.WailaConfig.ENABLED_TYPES.size > 1) {
            addSetting(
                CyclePlayerSettingButton(
                    rebarKey("cycle-waila-type"),
                    RebarConfig.WailaConfig.ENABLED_TYPES,
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