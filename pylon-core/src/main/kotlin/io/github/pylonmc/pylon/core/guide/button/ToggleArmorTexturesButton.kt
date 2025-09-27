package io.github.pylonmc.pylon.core.guide.button

import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.resourcepack.armor.ArmorTextureEngine.hasCustomArmorTextures
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import xyz.xenondevs.invui.item.impl.AbstractItem

class ToggleArmorTexturesButton : AbstractItem() {
    override fun getItemProvider(player: Player) = ItemStackBuilder.of(if (player.hasCustomArmorTextures) Material.LIME_CONCRETE else Material.RED_CONCRETE)
        .name(Component.translatable("pylon.pyloncore.guide.button.toggle-armor-textures.name"))
        .lore(Component.translatable("pylon.pyloncore.guide.button.toggle-armor-textures.lore"))

    override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
        player.hasCustomArmorTextures = !player.hasCustomArmorTextures
        notifyWindows()
    }
}