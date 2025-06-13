package io.github.pylonmc.pylon.core.guide.pages

import io.github.pylonmc.pylon.core.guide.pages.base.GuidePage
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.recipe.RecipeType
import net.kyori.adventure.text.Component
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.Gui

open class RecipePage<T: Keyed> (
    val type: RecipeType<T>,
    val recipe: T
) : GuidePage {

    // This page is never directly pointed to
    override val item = ItemStackBuilder.of(Material.BARRIER)

    override val title: Component
        get() = Component.empty()

    override fun getGui(player: Player): Gui = type.display(recipe)

    override fun getKey(): NamespacedKey = recipe.key
}