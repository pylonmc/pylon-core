package io.github.pylonmc.pylon.core.item

import io.github.pylonmc.pylon.core.recipe.RecipeType
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import java.util.function.Function

open class SimpleItemSchema<R : Keyed>
    @SafeVarargs
    constructor(
        id: NamespacedKey,
        template: ItemStack,
        private val recipeType: RecipeType<R>,
        vararg val recipes: (ItemStack) -> R,
    ) : PylonItemSchema(id, SimplePylonItem::class.java, template) {

    constructor(
        id: NamespacedKey,
        templateSupplier: Function<NamespacedKey, ItemStack>,
        recipeType: RecipeType<R>,
        recipe: (ItemStack) -> R,
    ) : this(id, templateSupplier.apply(id), recipeType, recipe)

    private var recipeKeys = mutableListOf<NamespacedKey>()

    override fun onRegister(registry: PylonRegistry<*>) {
        super.onRegister(registry)
        for (recipe in recipes) {
            val recipeInstance = recipe(itemStack)
            recipeKeys.add(recipeInstance.key)
            recipeType.addRecipe(recipeInstance)
        }
    }

    override fun onUnregister(registry: PylonRegistry<*>) {
        for (key in recipeKeys) {
            recipeType.removeRecipe(key)
        }
    }
}