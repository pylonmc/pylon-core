package io.github.pylonmc.pylon.core.block.context

import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

interface BlockCreateContext : BlockContext {

    data class PlayerPlace(override val block: Block, val player: Player, val item: ItemStack) : BlockCreateContext

    data class PluginPlace(override val block: Block) : BlockCreateContext

    data class PhantomBlockCreate(override val block: Block) : BlockCreateContext
}