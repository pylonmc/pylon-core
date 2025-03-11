package io.github.pylonmc.pylon.core.block

import org.bukkit.entity.Player

/**
 * Represents the reason why an item is being requested from a block
 */
sealed interface BlockItemReason {

    /**
     * The item is being requested because the block is being broken
     */
    data class Break(val context: BlockBreakContext) : BlockItemReason

    /**
     * The item is being requested because a player used the pick block button
     */
    // TODO implement
    data class PickBlock(val player: Player) : BlockItemReason
}
