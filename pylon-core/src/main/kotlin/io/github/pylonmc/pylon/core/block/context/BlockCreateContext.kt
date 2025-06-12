package io.github.pylonmc.pylon.core.block.context

import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

interface BlockCreateContext {

    val block: Block

    data class PlayerPlace(
        val player: Player,
        val item: ItemStack,
        override val block: Block,
        val clickedFace: BlockFace
    ) : BlockCreateContext

    data class Default(override val block: Block) : BlockCreateContext
}