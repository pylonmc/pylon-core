package io.github.pylonmc.pylon.core.block

import org.bukkit.entity.Player

/**
 * Represents the reason why an item is being requested from a block
 */
sealed interface BlockItemContext {

    /**
     * The item is being requested because the block is being broken
     */
    data class Break(val context: BlockBreakContext) : BlockItemContext

    /**
     * The item is being requested because a player used the pick block button
     */
    data class PickBlock(val player: Player) : BlockItemContext
}
