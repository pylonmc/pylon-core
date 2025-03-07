package io.github.pylonmc.pylon.core.block

import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockBurnEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.BlockFadeEvent

interface BlockItemReason {

    /**
     * Determines the default behavior of the reason
     */
    val normallyDrops: Boolean

    data object PluginBreak : BlockItemReason {
        override val normallyDrops = true
    }

    data class PlayerBreak(val event: BlockBreakEvent) : BlockItemReason {
        override val normallyDrops = true
    }

    // No event parameter since explosion can be either from block or entity
    data object WasExploded : BlockItemReason {
        override val normallyDrops = true
    }

    data class Exploded(val event: BlockExplodeEvent) : BlockItemReason {
        override val normallyDrops = false
    }

    data class Burned(val event: BlockBurnEvent) : BlockItemReason {
        override val normallyDrops = false
    }

    data class Faded(val event: BlockFadeEvent) : BlockItemReason {
        override val normallyDrops = false
    }

    // TODO implement
    data class PickBlock(val player: Player) : BlockItemReason {
        override val normallyDrops = true
    }
}