package io.github.pylonmc.pylon.core.block.context

import org.bukkit.GameMode
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockBurnEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.BlockFadeEvent
import org.bukkit.event.entity.EntityExplodeEvent

/**
 * Represents the context in which a block is being broken
 */
interface BlockBreakContext {

    /**
     * Determines the default drop behavior of the context
     */
    val normallyDrops: Boolean

    /**
     * The block is being broken by a plugin
     */
    data object PluginBreak : BlockBreakContext {
        override val normallyDrops = true
    }

    /**
     * The block is being broken by a player
     */
    data class PlayerBreak(val event: BlockBreakEvent) : BlockBreakContext {
        override val normallyDrops
            get() = event.player.gameMode != GameMode.CREATIVE
    }

    /**
     * The block is being exploded by an entity (e.g. a creeper or TNT)
     */
    data class EntityExploded(val event: EntityExplodeEvent) : BlockBreakContext {
        override val normallyDrops = true
    }

    /**
     * The block is exploding (e.g. beds in the Nether/End, respawn anchors in the Overworld/End)
     */
    data class BlockExplosionOrigin(val event: BlockExplodeEvent) : BlockBreakContext {
        override val normallyDrops = false
    }

    /**
     * The block is being exploded as a result of another block exploding
     */
    data class BlockExploded(val event: BlockExplodeEvent) : BlockBreakContext {
        override val normallyDrops = false
    }

    /**
     * The block has been burned away by fire
     */
    data class Burned(val event: BlockBurnEvent) : BlockBreakContext {
        override val normallyDrops = false
    }

    /**
     * The block has faded away (see [BlockFadeEvent] for more details)
     */
    data class Faded(val event: BlockFadeEvent) : BlockBreakContext {
        override val normallyDrops = false
    }
}