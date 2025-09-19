package io.github.pylonmc.pylon.core.guide.button

import io.github.pylonmc.pylon.core.block.textures.BlockTextureEngine.cullingPreset
import io.github.pylonmc.pylon.core.i18n.PylonArgument
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AbstractItem

class CullingPresetButton : AbstractItem() {
    override fun getItemProvider(player: Player): ItemProvider? {
        val preset = player.cullingPreset
        return ItemProvider {
            ItemStackBuilder.of(preset.material)
                .name(Component.translatable("pylon.pyloncore.guide.button.culling-preset.name.${preset.id}"))
                .lore(
                    Component.translatable("pylon.pyloncore.guide.button.culling-preset.lore.${preset.id}")
                        .arguments(
                            PylonArgument.of("hiddenInterval", preset.hiddenInterval),
                            PylonArgument.of("visibleInterval", preset.visibleInterval),
                            PylonArgument.of("alwaysShowRadius", preset.alwaysShowRadius),
                            PylonArgument.of("cullRadius", preset.cullRadius),
                            PylonArgument.of("maxOccludingCount", preset.maxOccludingCount)
                        )
                )
                .build()
        }
    }

    override fun handleClick(clickType: org.bukkit.event.inventory.ClickType, player: org.bukkit.entity.Player, event: org.bukkit.event.inventory.InventoryClickEvent) {
        TODO("Not yet implemented")
    }
}