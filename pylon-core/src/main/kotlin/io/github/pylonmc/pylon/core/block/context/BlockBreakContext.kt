package io.github.pylonmc.pylon.core.block.context

import org.bukkit.GameMode
import org.bukkit.block.Block
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockBurnEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.BlockFadeEvent

interface BlockBreakContext : BlockContext {

    /**
     * Determines the default behavior of the context
     */
    val normallyDrops: Boolean

    data class PluginBreak(override val block: Block) : BlockBreakContext {
        override val normallyDrops = true
    }

    data class PlayerBreak(override val block: Block, val event: BlockBreakEvent) : BlockBreakContext {
        override val normallyDrops
            get() = event.player.gameMode != GameMode.CREATIVE
    }

    // No event parameter since explosion can be either from block or entity
    data class WasExploded(override val block: Block) : BlockBreakContext {
        override val normallyDrops = true
    }

    data class Exploded(override val block: Block, val event: BlockExplodeEvent) : BlockBreakContext {
        override val normallyDrops = false
    }

    data class Burned(override val block: Block, val event: BlockBurnEvent) : BlockBreakContext {
        override val normallyDrops = false
    }

    data class Faded(override val block: Block, val event: BlockFadeEvent) : BlockBreakContext {
        override val normallyDrops = false
    }
}