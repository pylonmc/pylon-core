package io.github.pylonmc.rebar.guide.pages.settings

import io.github.pylonmc.rebar.config.PylonConfig
import io.github.pylonmc.rebar.guide.button.setting.CyclePlayerSettingButton
import io.github.pylonmc.rebar.guide.button.setting.TogglePlayerSettingButton
import io.github.pylonmc.rebar.i18n.PylonArgument
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder
import io.github.pylonmc.rebar.resourcepack.block.BlockTextureEngine.cullingPreset
import io.github.pylonmc.rebar.resourcepack.block.BlockTextureEngine.hasCustomBlockTextures
import io.github.pylonmc.rebar.util.rebarKey

object BlockTextureSettingsPage : PlayerSettingsPage(rebarKey("block_texture_settings")) {
    init {
        if (!PylonConfig.BlockTextureConfig.FORCED) {
            addSetting(
                TogglePlayerSettingButton(
                    rebarKey("toggle-block-textures"),
                    toggle = { player -> player.hasCustomBlockTextures = !player.hasCustomBlockTextures },
                    isEnabled = { player -> player.hasCustomBlockTextures }
                ))
        }
        addSetting(
            CyclePlayerSettingButton(
                rebarKey("cycle-culling-preset"),
                PylonConfig.BlockTextureConfig.CULLING_PRESETS.values.sortedBy { it.index },
                identifier = { preset -> preset.id },
                getter = { player -> player.cullingPreset },
                setter = { player, preset -> player.cullingPreset = preset },
                decorator = { player, preset ->
                    ItemStackBuilder.Companion.of(preset.material)
                        .addCustomModelDataString("culling_preset=${preset.id}")
                        .build()
                },
                placeholderProvider = { player, preset ->
                    mutableListOf(
                        PylonArgument.Companion.of("hiddenInterval", preset.hiddenInterval),
                        PylonArgument.Companion.of("visibleInterval", preset.visibleInterval),
                        PylonArgument.Companion.of("alwaysShowRadius", preset.alwaysShowRadius),
                        PylonArgument.Companion.of("cullRadius", preset.cullRadius),
                        PylonArgument.Companion.of("maxOccludingCount", preset.maxOccludingCount)
                    )
                }
            ))
    }
}