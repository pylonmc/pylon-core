package io.github.pylonmc.pylon.core.block.context

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * The reason a Pylon block was created
 */
interface BlockCreateContext {

    /**
     * A player placed the block
     */
    data class PlayerPlace(val player: Player, val item: ItemStack) : BlockCreateContext

    /**
     * No other reason was given
     */
    data object Default : BlockCreateContext
}