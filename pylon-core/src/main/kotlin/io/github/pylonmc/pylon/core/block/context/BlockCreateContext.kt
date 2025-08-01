package io.github.pylonmc.pylon.core.block.context

import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.ItemStack

interface BlockCreateContext {

    val block: Block

    /**
     * A player has placed the block
     */
    data class PlayerPlace(
        val player: Player,
        val item: ItemStack,
        val event: BlockPlaceEvent
    ) : BlockCreateContext {
        override val block = event.blockPlaced
    }

    /**
     * A context in which no other reason is specified
     */
    data class Default(override val block: Block) : BlockCreateContext
}