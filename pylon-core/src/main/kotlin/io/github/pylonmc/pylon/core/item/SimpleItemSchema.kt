package io.github.pylonmc.pylon.core.item

import io.github.pylonmc.pylon.core.block.PylonBlockSchema
import io.github.pylonmc.pylon.core.recipe.RecipeType
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import javax.xml.stream.events.Namespace

open class SimpleItemSchema<R : Keyed> @JvmOverloads constructor(
    id: NamespacedKey,
    template: ItemStack,
    private val recipeType: RecipeType<R>,
    private val recipe: (ItemStack) -> R,
    private val block: PylonBlockSchema? = null
) : PylonItemSchema(id, SimplePylonItem::class.java, template) {
    constructor(id: NamespacedKey, template: () -> ItemStack, recipeType: RecipeType<R>,
                recipe: (ItemStack) -> R, block: PylonBlockSchema? = null)
            : this(id, template.invoke(), recipeType, recipe, block)

    private var recipeKey: NamespacedKey? = null

    override fun onRegister(registry: PylonRegistry<*>) {
        super.onRegister(registry)
        val recipeInstance = recipe(itemStack)
        recipeKey = recipeInstance.key
        recipeType.addRecipe(recipeInstance)
        block?.register()
    }

    override fun onUnregister(registry: PylonRegistry<*>) {
        if (recipeKey != null) {
            recipeType.removeRecipe(recipeKey!!)
            recipeKey = null
        }
        if (block != null) {
            PylonRegistry.BLOCKS.unregister(block.key)
        }
    }
}