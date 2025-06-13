package io.github.pylonmc.pylon.core.recipe

import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.fluid.PylonFluid
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.item.base.VanillaCraftingItem
import io.github.pylonmc.pylon.core.item.base.VanillaSmithingMaterial
import io.github.pylonmc.pylon.core.item.base.VanillaSmithingTemplate
import org.bukkit.Bukkit
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockCookEvent
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.event.inventory.PrepareSmithingEvent
import org.bukkit.inventory.*
import xyz.xenondevs.invui.gui.Gui

object RecipeTypes {

    @JvmField
    @Suppress("UNCHECKED_CAST")
    val VANILLA_BLASTING: RecipeType<BlastingRecipeWrapper> = CookingRecipeType("blasting", Material.BLAST_FURNACE) as RecipeType<BlastingRecipe>

    @JvmField
    @Suppress("UNCHECKED_CAST")
    val VANILLA_CAMPFIRE: RecipeType<CampfireRecipe> = CookingRecipeType("campfire", Material.CAMPFIRE) as RecipeType<CampfireRecipe>

    @JvmField
    val VANILLA_SHAPED: RecipeType<ShapedRecipeWrapper> = CraftingRecipeType("shaped")

    @JvmField
    val VANILLA_SHAPELESS: RecipeType<ShapelessRecipeWrapper> = CraftingRecipeType("shapeless")

    @JvmField
    @Suppress("UNCHECKED_CAST")
    val VANILLA_FURNACE: RecipeType<FurnaceRecipe> = CookingRecipeType("furnace", Material.FURNACE) as RecipeType<FurnaceRecipe>

    @JvmField
    val VANILLA_SMITHING: RecipeType<SmithingRecipe> = SmithingRecipeType

    @JvmField
    @Suppress("UNCHECKED_CAST")
    val VANILLA_SMOKING: RecipeType<SmokingRecipe> = CookingRecipeType("smoking", Material.SMOKER) as RecipeType<SmokingRecipe>

    init {
        VANILLA_BLASTING.register()
        VANILLA_CAMPFIRE.register()
        VANILLA_SHAPED.register()
        VANILLA_SHAPELESS.register()
        VANILLA_FURNACE.register()
        VANILLA_SMITHING.register()
        VANILLA_SMOKING.register()
    }
}

abstract class CraftingRecipeWrapper : PylonRecipe {
    // TODO gui
}

class ShapedRecipeWrapper(val recipe: ShapedRecipe) : CraftingRecipeWrapper() {
    override fun getKey(): NamespacedKey = recipe.key

    override fun getResult(): ItemStack = recipe.result

    override fun getInputItems(): Set<ItemStack> = recipe.choiceMap.values.map { it.itemStack }.toSet()
    override fun getOutputItems(): Set<ItemStack> = setOf(recipe.result)

    override fun getInputFluids(): Set<PylonFluid> = setOf()
    override fun getOutputFluids(): Set<PylonFluid> = setOf()
}

class ShapelessRecipeWrapper(val recipe: ShapelessRecipe) : CraftingRecipeWrapper() {
    override fun getKey(): NamespacedKey = recipe.key

    override fun getResult(): ItemStack = recipe.result

    override fun getInputItems(): Set<ItemStack> = recipe.choiceList.map { it.itemStack }.toSet()
    override fun getOutputItems(): Set<ItemStack> = setOf(recipe.result)

    override fun getInputFluids(): Set<PylonFluid> = setOf()
    override fun getOutputFluids(): Set<PylonFluid> = setOf()
}

private class CraftingRecipeType<T: PylonRecipe>(key: String) : VanillaRecipe<T>(key) {

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onPreCraft(e: PrepareItemCraftEvent) {
        val recipe = e.recipe
        // All recipe types but MerchantRecipe implement Keyed
        if (recipe !is Keyed) return
        val inventory = e.inventory
        if (recipes.all { it.key != recipe.key } && inventory.any { it.isPylonAndIsNot<VanillaCraftingItem>() }) {
            // Prevent the erroneous crafting of vanilla items with Pylon ingredients
            inventory.result = null
        }
    }
}

abstract class CookingRecipeWrapper(
    val recipe: CookingRecipe<*>,
    val block: Material
) : PylonRecipe {
    override fun getKey(): NamespacedKey = recipe.key

    override fun getResult(): ItemStack = recipe.result

    override fun getInputItems(): Set<ItemStack> = setOf(recipe.input)
    override fun getOutputItems(): Set<ItemStack> = setOf(recipe.result)

    override fun getInputFluids(): Set<PylonFluid> = setOf()
    override fun getOutputFluids(): Set<PylonFluid> = setOf()

    override fun display(): Gui {
        return Gui.normal()
            .setStructure(
                "# # # # # # # # #",
                "# b # i f o # c #",
                "# # # # # # # # #",
            )
            .addIngredient('b', ItemStack(block))
            .addIngredient('i', recipe.inputChoice.itemStack)
            .addIngredient('f', ItemStack(Material.FIRE))
            .addIngredient('o', recipe.result)
            .addIngredient('c', ItemStack(Material.COAL))
            .build()
    }
}

class BlastingRecipeWrapper(recipe: BlastingRecipe) : CookingRecipeWrapper(recipe, Material.BLAST_FURNACE)

class RecipeWrapper(recipe: BlastingRecipe) : CookingRecipeWrapper(recipe, Material.BLAST_FURNACE)

class BlastingRecipeWrapper(recipe: BlastingRecipe) : CookingRecipeWrapper(recipe, Material.BLAST_FURNACE)

private class CookingRecipeType(
    key: String,
) : VanillaRecipe<CookingRecipeWrapper>(key) {

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onCook(e: BlockCookEvent) {
        val input = e.source
        if (PylonItem.fromStack(input) == null) return
        for (recipe in recipes) {
            if (recipe.recipe.inputChoice.test(input)) {
                e.result = recipe.recipe.result.clone()
                return
            }
        }
    }
}

private object SmithingRecipeType : VanillaRecipe<SmithingRecipe>("smithing") {

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onSmith(e: PrepareSmithingEvent) {
        val inv = e.inventory
        val recipe = inv.recipe
        if (recipe !is Keyed) return
        if (
            recipes.all { it.key != recipe.key } &&
            (
                    inv.inputMineral.isPylonAndIsNot<VanillaSmithingMaterial>() ||
                    inv.inputTemplate.isPylonAndIsNot<VanillaSmithingTemplate>()
            )
        ) {
            // Prevent the erroneous smithing of vanilla items with Pylon ingredients
            inv.result = null
        }
    }
}

private abstract class VanillaRecipe<T>(key: String)
    : RecipeType<T>(NamespacedKey.minecraft(key)), Listener
        where T : Keyed, T : PylonRecipe {

    init {
        Bukkit.getPluginManager().registerEvents(this, PylonCore)
    }

    override fun addRecipe(recipe: T) {
        super.addRecipe(recipe)
        Bukkit.addRecipe(recipe)
    }

    override fun removeRecipe(recipe: NamespacedKey) {
        super.removeRecipe(recipe)
        Bukkit.removeRecipe(recipe)
    }
}

private inline fun <reified T> ItemStack?.isPylonAndIsNot(): Boolean {
    val pylonItem = PylonItem.fromStack(this)
    return pylonItem != null && pylonItem !is T
}