package io.github.pylonmc.pylon.core.block.context

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

interface BlockCreateContext {

    data class PlayerPlace(val player: Player, val item: ItemStack) : BlockCreateContext

    data object Default : BlockCreateContext
}