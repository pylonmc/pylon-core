package io.github.pylonmc.pylon.core.block.context

import io.github.pylonmc.pylon.core.block.BlockStorage
import org.bukkit.GameMode
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockBurnEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.BlockFadeEvent

/**
 * The reason a Pylon block was broken
 */
interface BlockBreakContext {

    /**
     * Determines the default behavior of the context
     */
    val normallyDrops: Boolean

    /**
     * The block was removed by a plugin calling [BlockStorage.breakBlock]
     */
    data object PluginBreak : BlockBreakContext {
        override val normallyDrops = true
    }

    data class PlayerBreak(val event: BlockBreakEvent) : BlockBreakContext {
        override val normallyDrops
            get() = event.player.gameMode != GameMode.CREATIVE
    }

    /**
     * Unlike [Exploded], this context means that the block was exploded by an entity
     * or another block exploding it
     */
    // No event parameter since explosion can be either from block or entity
    data object WasExploded : BlockBreakContext {
        override val normallyDrops = true
    }

    /**
     * Unlike [WasExploded], this context means that the *block itself* exploded,
     * i.e., the block was a TNT
     */
    data class Exploded(val event: BlockExplodeEvent) : BlockBreakContext {
        override val normallyDrops = false
    }

    data class Burned(val event: BlockBurnEvent) : BlockBreakContext {
        override val normallyDrops = false
    }

    /**
     * See [BlockFadeEvent] for information on what counts as "fading"
     */
    data class Faded(val event: BlockFadeEvent) : BlockBreakContext {
        override val normallyDrops = false
    }
}