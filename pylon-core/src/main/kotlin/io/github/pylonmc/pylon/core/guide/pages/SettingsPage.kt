package io.github.pylonmc.pylon.core.guide.pages

import io.github.pylonmc.pylon.core.config.PylonConfig
import io.github.pylonmc.pylon.core.content.guide.PylonGuide
import io.github.pylonmc.pylon.core.guide.button.BackButton
import io.github.pylonmc.pylon.core.guide.button.PageButton
import io.github.pylonmc.pylon.core.guide.button.setting.CycleSettingButton
import io.github.pylonmc.pylon.core.guide.button.setting.ToggleSettingButton
import io.github.pylonmc.pylon.core.guide.pages.base.SimpleStaticGuidePage
import io.github.pylonmc.pylon.core.i18n.PylonArgument
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.item.research.Research.Companion.researchEffects
import io.github.pylonmc.pylon.core.resourcepack.block.BlockTextureEngine.cullingPreset
import io.github.pylonmc.pylon.core.resourcepack.block.BlockTextureEngine.hasCustomBlockTextures
import io.github.pylonmc.pylon.core.util.gui.GuiItems
import io.github.pylonmc.pylon.core.util.pylonKey
import io.github.pylonmc.pylon.core.waila.Waila.Companion.wailaConfig
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item

/**
 * Contains buttons to change settings.
 */
class SettingsPage(
    key: NamespacedKey = pylonKey("settings"),
    material: Material = Material.COMPARATOR,
    buttons: MutableList<Item> = mutableListOf(),
) : SimpleStaticGuidePage(key, material, buttons) {
    override fun getGui(player: Player): Gui {
        val buttons = buttonSupplier.get()
        val gui = PagedGui.items()
            .setStructure(
                "# b # # # # # s #",
                "# # # # # # # # #",
                "# x x x x x x x #",
                "# # # # # # # # #",
            )
            .addIngredient('#', GuiItems.background())
            .addIngredient('b', BackButton())
            .addIngredient('s', PageButton(PylonGuide.searchItemsAndFluidsPage))
            .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            .addPageChangeHandler { _, newPage -> saveCurrentPage(player, newPage) }
            .setContent(buttons.filter { it !is PageButton || it.page !is SettingsPage || it.page.buttons.isNotEmpty() })
        return gui.build().apply { loadCurrentPage(player, this) }
    }

    fun addSetting(item: Item) {
        buttons.add(item)
        buttons.sortBy { if (it is PageButton) 0 else 1 }
    }

    companion object {
        @JvmStatic
        val wailaSettings = SettingsPage(
            pylonKey("waila_settings"),
            Material.SPYGLASS
        ).apply {
            addSetting(ToggleSettingButton(
                pylonKey("toggle-waila"),
                toggle = { player -> player.wailaConfig.enabled = !player.wailaConfig.enabled },
                isEnabled = { player -> player.wailaConfig.enabled }
            ))
            addSetting(ToggleSettingButton(
                pylonKey("toggle-vanilla-waila"),
                toggle = { player -> player.wailaConfig.vanillaWailaEnabled = !player.wailaConfig.vanillaWailaEnabled },
                isEnabled = { player -> player.wailaConfig.vanillaWailaEnabled }
            ))
            if (PylonConfig.WailaConfig.enabledTypes.size > 1) {
                addSetting(CycleSettingButton(
                    pylonKey("cycle-waila-type"),
                    PylonConfig.WailaConfig.enabledTypes,
                    identifier = { type -> type.name.lowercase() },
                    getter = { player -> player.wailaConfig.type },
                    setter = { player, type -> player.wailaConfig.type = type },
                    decorator = { player, type -> ItemStackBuilder.of(Material.PAPER)
                        .addCustomModelDataString("waila_type=${type.name.lowercase()}")
                        .build()
                    }
                ))
            }
        }

        @JvmStatic
        val resourcePackSettings = SettingsPage(
            pylonKey("resource_pack_settings"),
            Material.PAINTING
        )

        @JvmStatic
        val blockTextureSettings = SettingsPage(
            pylonKey("block_texture_settings"),
            Material.BOOKSHELF
        ).apply {
            if (!PylonConfig.BlockTextureConfig.forced) {
                addSetting(ToggleSettingButton(
                    pylonKey("toggle-block-textures"),
                    toggle = { player -> player.hasCustomBlockTextures = !player.hasCustomBlockTextures },
                    isEnabled = { player -> player.hasCustomBlockTextures }
                ))
            }
            addSetting(CycleSettingButton(
                pylonKey("cycle-culling-preset"),
                PylonConfig.BlockTextureConfig.cullingPresets.values.sortedBy { it.index },
                identifier = { preset -> preset.id },
                getter = { player -> player.cullingPreset },
                setter = { player, preset -> player.cullingPreset = preset },
                decorator = { player, preset -> ItemStackBuilder.of(preset.material)
                    .addCustomModelDataString("culling_preset=${preset.id}")
                    .build()
                },
                placeholderProvider = { player, preset -> mutableListOf(
                    PylonArgument.of("hiddenInterval", preset.hiddenInterval),
                    PylonArgument.of("visibleInterval", preset.visibleInterval),
                    PylonArgument.of("alwaysShowRadius", preset.alwaysShowRadius),
                    PylonArgument.of("cullRadius", preset.cullRadius),
                    PylonArgument.of("maxOccludingCount", preset.maxOccludingCount)
                )}
            ))
        }

        @JvmStatic
        val researchEffects = ToggleSettingButton(
            pylonKey("toggle-research-effects"),
            toggle = { player -> player.researchEffects = !player.researchEffects },
            isEnabled = { player -> player.researchEffects }
        )
    }
}