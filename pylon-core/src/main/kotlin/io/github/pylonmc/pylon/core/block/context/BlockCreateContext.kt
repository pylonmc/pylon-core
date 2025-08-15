package io.github.pylonmc.pylon.core.block.context

import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin

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
     * A plugin generated the block
     * ex:
     * - Growing of Pylon Trees
     * - Evolution of Pylon Sponges
     */
    data class PluginGenerate(
        val plugin: Plugin,
        override val block: Block,
        val item: ItemStack
    ) : BlockCreateContext

    /**
     * A context in which no other reason is specified
     */
    data class Default(override val block: Block) : BlockCreateContext
}