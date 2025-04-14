package io.github.pylonmc.pylon.core.block.context

import org.bukkit.GameMode
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockBurnEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.BlockFadeEvent

interface BlockBreakContext {

    /**
     * Determines the default behavior of the context
     */
    val normallyDrops: Boolean

    data object PluginBreak : BlockBreakContext {
        override val normallyDrops = true
    }

    data class PlayerBreak(val event: BlockBreakEvent) : BlockBreakContext {
        override val normallyDrops
            get() = event.player.gameMode != GameMode.CREATIVE
    }

    // No event parameter since explosion can be either from block or entity
    data object WasExploded : BlockBreakContext {
        override val normallyDrops = true
    }

    data class Exploded(val event: BlockExplodeEvent) : BlockBreakContext {
        override val normallyDrops = false
    }

    data class Burned(val event: BlockBurnEvent) : BlockBreakContext {
        override val normallyDrops = false
    }

    data class Faded(val event: BlockFadeEvent) : BlockBreakContext {
        override val normallyDrops = false
    }
}