package io.github.pylonmc.rebar.event

import io.github.pylonmc.rebar.waila.Waila
import io.github.pylonmc.rebar.waila.WailaDisplay
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent

/**
 * Called when the players [WAILA display][WailaDisplay] is being generated for a targeted [Block].
 * This is called if and only if the player has WAILA enabled and the block already has a generated display.
 *
 * @see Waila
 */
class RebarBlockWailaEvent(
    player: Player,
    val block: Block,
    var display: WailaDisplay?
) : PlayerEvent(player), Cancellable {
    private var cancelled = false

    override fun isCancelled(): Boolean = cancelled
    override fun setCancelled(cancel: Boolean) {
        cancelled = cancel
    }

    override fun getHandlers(): HandlerList = handlerList

    companion object {
        @JvmStatic
        val handlerList: HandlerList = HandlerList()
    }
}