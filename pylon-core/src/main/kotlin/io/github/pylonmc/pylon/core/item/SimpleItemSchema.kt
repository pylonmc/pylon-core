package io.github.pylonmc.pylon.core.item

import io.github.pylonmc.pylon.core.block.PylonBlockSchema
import io.github.pylonmc.pylon.core.recipe.RecipeType
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack

open class SimpleItemSchema<R : Keyed>(
    id: NamespacedKey,
    template: ItemStack,
    private val recipeType: RecipeType<R>,
    private val recipe: (ItemStack) -> R,
) : PylonItemSchema(id, SimplePylonItem::class.java, template) {

    private var recipeKey: NamespacedKey? = null

    override fun onRegister(registry: PylonRegistry<*>) {
        super.onRegister(registry)
        val recipeInstance = recipe(itemStack)
        recipeKey = recipeInstance.key
        recipeType.addRecipe(recipeInstance)
    }

    override fun onUnregister(registry: PylonRegistry<*>) {
        if (recipeKey != null) {
            recipeType.removeRecipe(recipeKey!!)
            recipeKey = null
        }
    }
}