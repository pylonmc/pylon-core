package io.github.pylonmc.pylon.core.block.context

import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin

interface BlockCreateContext {

    /**
     * The block at the position where the context is created
     */
    val block: Block

    /**
     * If true, the type of the block will be set to the type of the Pylon block
     */
    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("shouldSetType")
    val shouldSetType: Boolean
        get() = true

    /**
     * A player has placed the block
     */
    data class PlayerPlace(
        val player: Player,
        val item: ItemStack,
        val event: BlockPlaceEvent
    ) : BlockCreateContext {
        override val block = event.blockPlaced
        override val shouldSetType = false // The action of the placement sets the block
    }

    /**
     * A plugin generated the block
     * ex:
     * - Growing of Pylon Trees
     * - Evolution of Pylon Sponges
     */
    @JvmRecord
    data class PluginGenerate(
        val plugin: Plugin,
        override val block: Block,
        val item: ItemStack
    ) : BlockCreateContext

    /**
     * A context in which no other reason is specified
     */
    @JvmRecord
    data class Default @JvmOverloads constructor(
        override val block: Block,
        override val shouldSetType: Boolean = true
    ) : BlockCreateContext
}