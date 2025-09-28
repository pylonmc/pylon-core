package io.github.pylonmc.pylon.core.guide.button

import io.github.pylonmc.pylon.core.resourcepack.block.BlockTextureEngine.hasCustomBlockTextures
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.resourcepack.block.BlockTextureEngine
import io.github.pylonmc.pylon.core.util.pylonKey
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import xyz.xenondevs.invui.item.impl.AbstractItem

class ToggleBlockTexturesButton : AbstractItem() {
    override fun getItemProvider(player: Player)
        = ItemStackBuilder.gui(if (player.hasCustomBlockTextures) Material.LIME_CONCRETE else Material.RED_CONCRETE, pylonKey("toggle_block_textures"))
            .name(Component.translatable("pylon.pyloncore.guide.button.toggle-block-textures.name"))
            .lore(Component.translatable("pylon.pyloncore.guide.button.toggle-block-textures.lore"))

    override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
        player.hasCustomBlockTextures = !player.hasCustomBlockTextures
        if (player.hasCustomBlockTextures) {
            BlockTextureEngine.launchBlockTextureJob(player)
        }
        notifyWindows()
    }
}