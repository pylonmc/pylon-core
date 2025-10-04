package io.github.pylonmc.pylon.core.guide.button

import io.github.pylonmc.pylon.core.resourcepack.block.BlockTextureEngine.cullingPreset
import io.github.pylonmc.pylon.core.i18n.PylonArgument
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.resourcepack.block.BlockTextureConfig
import io.github.pylonmc.pylon.core.util.pylonKey
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AbstractItem

class CullingPresetButton : AbstractItem() {
    override fun getItemProvider(player: Player): ItemProvider? {
        val preset = player.cullingPreset
        return ItemStackBuilder.gui(preset.material, pylonKey("guide_culling_preset_${preset.id}"))
            .name(Component.translatable("pylon.pyloncore.guide.button.culling-preset.${preset.id}.name"))
            .lore(
                Component.translatable("pylon.pyloncore.guide.button.culling-preset.${preset.id}.lore")
                    .arguments(
                        PylonArgument.of("hiddenInterval", preset.hiddenInterval),
                        PylonArgument.of("visibleInterval", preset.visibleInterval),
                        PylonArgument.of("alwaysShowRadius", preset.alwaysShowRadius),
                        PylonArgument.of("cullRadius", preset.cullRadius),
                        PylonArgument.of("maxOccludingCount", preset.maxOccludingCount)
                    )
            )
    }

    override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
        val presets = BlockTextureConfig.cullingPresets.values.toMutableList()
        presets.sortBy { it.index }
        val currentIndex = presets.indexOfFirst { it.id == player.cullingPreset.id }
        val nextIndex = (currentIndex + 1) % presets.size
        player.cullingPreset = presets[nextIndex]
        notifyWindows()
    }
}