package io.github.pylonmc.pylon.core.recipe

import com.destroystokyo.paper.event.player.PlayerRecipeBookClickEvent
import io.github.pylonmc.pylon.core.nms.NmsAccessor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

object RecipeCompletion : Listener {

    @EventHandler
    private fun onRecipeBookClick(e: PlayerRecipeBookClickEvent) = NmsAccessor.instance.handleRecipeBookClick(e)
}