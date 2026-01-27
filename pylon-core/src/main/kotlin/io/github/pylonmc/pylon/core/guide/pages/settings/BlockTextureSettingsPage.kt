package io.github.pylonmc.pylon.core.guide.pages.settings

import io.github.pylonmc.pylon.core.config.PylonConfig
import io.github.pylonmc.pylon.core.guide.button.setting.CyclePlayerSettingButton
import io.github.pylonmc.pylon.core.guide.button.setting.TogglePlayerSettingButton
import io.github.pylonmc.pylon.core.i18n.PylonArgument
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.resourcepack.block.BlockTextureEngine.cullingPreset
import io.github.pylonmc.pylon.core.resourcepack.block.BlockTextureEngine.hasCustomBlockTextures
import io.github.pylonmc.pylon.core.util.pylonKey

object BlockTextureSettingsPage : PlayerSettingsPage(pylonKey("block_texture_settings")) {
    init {
        if (!PylonConfig.BlockTextureConfig.FORCED) {
            addSetting(
                TogglePlayerSettingButton(
                    pylonKey("toggle-block-textures"),
                    toggle = { player -> player.hasCustomBlockTextures = !player.hasCustomBlockTextures },
                    isEnabled = { player -> player.hasCustomBlockTextures }
                ))
        }
        addSetting(
            CyclePlayerSettingButton(
                pylonKey("cycle-culling-preset"),
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