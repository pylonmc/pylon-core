package io.github.pylonmc.pylon.core.nms.item

import io.papermc.paper.inventory.recipe.ItemOrExact
import net.minecraft.world.entity.player.StackedContents.IngredientInfo

fun wrapIngredients(ingredients: MutableList<out IngredientInfo<ItemOrExact?>?>):
        List<IngredientInfo<ItemOrExact?>?> {

    return ingredients/*.map { ing ->
        if (ing == null) return@map null

        IngredientInfo<ItemOrExact?> { actual: ItemOrExact? ->
            if (actual == null) return@IngredientInfo false

            when (actual) {

                is ItemOrExact.Exact -> {
                    ing.acceptsItem(actual)
                }

                is ItemOrExact.Item -> {
                    // should be always true from the look of it
                    if (ing !is Ingredient || ing.itemStacks() == null) {
                        return@IngredientInfo ing.acceptsItem(actual)
                    }

                    val stacks = ing.itemStacks()!!
                    for (stack in stacks) {
                        if (PylonItem.fromStack(stack.bukkitStack) != null) {
                            return@IngredientInfo false
                        }
                    }

                    return@IngredientInfo ing.acceptsItem(actual)
                }
            }
        }
    }*/
}
