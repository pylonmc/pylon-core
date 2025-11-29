package io.github.pylonmc.pylon.core.nms.recipe

import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.inventory.AbstractCraftingMenu
import net.minecraft.world.inventory.RecipeBookMenu
import net.minecraft.world.item.crafting.CraftingRecipe
import net.minecraft.world.item.crafting.RecipeHolder
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType

class HandlerRecipeBookClick(val player: ServerPlayer) {

    fun handlePylonItemPlacement(
        menu: AbstractCraftingMenu,
        useMaxItems: Boolean,
        recipe: RecipeHolder<*>?,
        level: ServerLevel?,
    ): RecipeBookMenu.PostPlaceAction {
        val recipeHolder = recipe as RecipeHolder<CraftingRecipe>

        init()

        beginPlacingRecipe.invokeExact(menu)
        var postPlaceAction: RecipeBookMenu.PostPlaceAction
        try {
            val inputGridSlots = menu.inputGridSlots
            postPlaceAction = PylonServerPlaceRecipe.Companion.placeRecipe(
                menu,
                player,
                inputGridSlots,
                inputGridSlots,
                recipeHolder,
                useMaxItems
            )
        } finally {
            finishPlacingRecipe.invokeExact(menu, level, recipe)
        }

        return postPlaceAction
    }

    companion object {

        var initialized = false
        lateinit var beginPlacingRecipe: MethodHandle
        lateinit var finishPlacingRecipe: MethodHandle

        fun init() {
            if (initialized) return

            initialized = true
            val lookup = MethodHandles.privateLookupIn(
                AbstractCraftingMenu::class.java,
                MethodHandles.lookup()
            )

            val beginPlacingRecipeType = MethodType.methodType(Void.TYPE)
            beginPlacingRecipe = lookup.findVirtual(AbstractCraftingMenu::class.java, "beginPlacingRecipe", beginPlacingRecipeType)

            val finishPlacingRecipeType = MethodType.methodType(Void.TYPE, ServerLevel::class.java, RecipeHolder::class.java)
            finishPlacingRecipe = lookup.findVirtual(AbstractCraftingMenu::class.java, "finishPlacingRecipe", finishPlacingRecipeType)
        }
    }
}