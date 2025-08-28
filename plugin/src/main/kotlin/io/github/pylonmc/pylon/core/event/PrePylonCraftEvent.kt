package io.github.pylonmc.pylon.core.event

import io.github.pylonmc.pylon.core.block.PylonBlock
import io.github.pylonmc.pylon.core.recipe.PylonRecipe
import io.github.pylonmc.pylon.core.recipe.RecipeType
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Called when a crafting recipe is started
 */
class PrePylonCraftEvent<T: PylonRecipe> @JvmOverloads constructor(
    val type: RecipeType<T>,
    val recipe: T,
    val block: PylonBlock? = null,
    val player: Player? = null
) : Event(), Cancellable {

    private var isCancelled = false

    override fun isCancelled(): Boolean = isCancelled

    override fun setCancelled(cancel: Boolean) {
        isCancelled = cancel
    }

    override fun getHandlers(): HandlerList
        = handlerList

    companion object {
        @JvmStatic
        val handlerList: HandlerList = HandlerList()
    }
}