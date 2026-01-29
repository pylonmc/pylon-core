package io.github.pylonmc.rebar.guide.pages.settings

import io.github.pylonmc.rebar.config.RebarConfig
import io.github.pylonmc.rebar.guide.button.setting.CyclePlayerSettingButton
import io.github.pylonmc.rebar.guide.button.setting.TogglePlayerSettingButton
import io.github.pylonmc.rebar.i18n.RebarArgument
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder
import io.github.pylonmc.rebar.resourcepack.block.BlockTextureEngine.cullingPreset
import io.github.pylonmc.rebar.resourcepack.block.BlockTextureEngine.hasCustomBlockTextures
import io.github.pylonmc.rebar.util.rebarKey

object BlockTextureSettingsPage : PlayerSettingsPage(rebarKey("block_texture_settings")) {
    init {
        if (!RebarConfig.BlockTextureConfig.FORCED) {
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
                RebarConfig.BlockTextureConfig.CULLING_PRESETS.values.sortedBy { it.index },
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
                        RebarArgument.Companion.of("hiddenInterval", preset.hiddenInterval),
                        RebarArgument.Companion.of("visibleInterval", preset.visibleInterval),
                        RebarArgument.Companion.of("alwaysShowRadius", preset.alwaysShowRadius),
                        RebarArgument.Companion.of("cullRadius", preset.cullRadius),
                        RebarArgument.Companion.of("maxOccludingCount", preset.maxOccludingCount)
                    )
                }
            ))
    }
}