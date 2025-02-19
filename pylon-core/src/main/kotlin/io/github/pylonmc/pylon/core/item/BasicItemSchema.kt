package io.github.pylonmc.pylon.core.item

import io.github.pylonmc.pylon.core.recipe.RecipeType
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack

class BasicItemSchema<R : Keyed>(
    id: NamespacedKey,
    template: ItemStack,
    recipeType: RecipeType<R>,
    recipe: (ItemStack) -> R,
) : PylonItemSchema(id, SimplePylonItem::class.java, template) {
    init {
        recipeType.addRecipe(recipe(template))
    }
}