package io.github.pylonmc.pylon.core.item

import io.github.pylonmc.pylon.core.recipe.RecipeType
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin

open class SimpleItemSchema<R : Keyed>(
    id: NamespacedKey,
    source: InitialItemSource,
    private val recipeType: RecipeType<R>,
    private val recipe: (ItemStack) -> R,
) : PylonItemSchema(id, SimplePylonItem::class.java, source) {

    constructor(
        key: NamespacedKey,
        template: ItemStack,
        recipeType: RecipeType<R>,
        recipe: (ItemStack) -> R,
    ) : this(key, InitialItemSource.ItemStack(template), recipeType, recipe)

    constructor(
        key: NamespacedKey,
        plugin: Plugin,
        recipeType: RecipeType<R>,
        recipe: (ItemStack) -> R,
    ) : this(key, InitialItemSource.File(plugin), recipeType, recipe)

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