package io.github.pylonmc.pylon.core.recipe

import io.github.pylonmc.pylon.core.fluid.PylonFluid
import io.github.pylonmc.pylon.core.item.ItemTypeWrapper
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.roundToInt

class IngredientCalculator private constructor() {

    private val ingredients = mutableMapOf<FluidOrItem, Double>()
    private val byproducts = mutableMapOf<FluidOrItem, Double>()

    private val blacklistedRecipes = baseRecipes.toMutableSet()

    private val calculationStack = ArrayDeque<NamespacedKey>()

    private fun calculateFor(input: FluidOrItem) {
        var input = input
        val oneInput = input.asOne()
        val unusedByproduct = byproducts[oneInput]
        if (unusedByproduct != null) {
            when (input) {
                is FluidOrItem.Fluid -> {
                    val toRemove = min(unusedByproduct, input.amountMillibuckets)
                    val remainingInput = input.amountMillibuckets - toRemove
                    val remainingByproduct = unusedByproduct - toRemove
                    if (remainingByproduct == 0.0) {
                        byproducts.remove(oneInput)
                    } else {
                        byproducts[oneInput] = remainingByproduct
                    }
                    if (remainingInput <= 0.0) {
                        return
                    } else {
                        input = input.copy(amountMillibuckets = remainingInput)
                    }
                }

                is FluidOrItem.Item -> {
                    val toRemove = min(unusedByproduct.roundToInt(), input.item.amount)
                    val remainingInput = input.item.amount - toRemove
                    val remainingByproduct = unusedByproduct - toRemove
                    if (remainingByproduct == 0.0) {
                        byproducts.remove(oneInput)
                    } else {
                        byproducts[oneInput] = remainingByproduct
                    }
                    if (remainingInput <= 0) {
                        return
                    } else {
                        input = input.copy(item = input.item.asQuantity(remainingInput))
                    }
                }
            }
        }

        if (oneInput in baseIngredients || (input is FluidOrItem.Item && ItemTypeWrapper(input.item) is ItemTypeWrapper.Vanilla)) {
            ingredients.merge(oneInput, input.amount, Double::plus)
            return
        }

        var recipe: PylonRecipe?
        do {
            recipe = when (input) {
                is FluidOrItem.Fluid -> findRecipeFor(input.fluid)
                is FluidOrItem.Item -> findRecipeFor(PylonItem.from(input.item)!!) // guaranteed to be a Pylon item because of the vanilla check above
            }

            if (recipe != null && recipe.key in calculationStack) {
                blacklistedRecipes.add(recipe)
            }
        } while (recipe in blacklistedRecipes)

        if (recipe == null) {
            ingredients.merge(oneInput, input.amount, Double::plus)
            return
        }

        val output = recipe.results.find { it.matchesType(input) }
        if (output == null) {
            ingredients.merge(oneInput, input.amount, Double::plus)
            return
        }

        calculationStack.addLast(recipe.key)

        val outputMulti = when (input) {
            is FluidOrItem.Fluid -> ceil(input.amountMillibuckets / (output as FluidOrItem.Fluid).amountMillibuckets).toInt()
            is FluidOrItem.Item -> Math.ceilDiv(input.item.amount, (output as FluidOrItem.Item).item.amount)
        }

        val extra = when (input) {
            is FluidOrItem.Fluid -> {
                val totalProduced = (output as FluidOrItem.Fluid).amountMillibuckets * outputMulti
                val extraAmount = totalProduced - input.amountMillibuckets
                if (extraAmount > 0) input.copy(amountMillibuckets = extraAmount) else null
            }

            is FluidOrItem.Item -> {
                val totalProduced = (output as FluidOrItem.Item).item.amount * outputMulti
                val extraAmount = totalProduced - input.item.amount
                if (extraAmount > 0) input.copy(item = input.item.asQuantity(extraAmount)) else null
            }
        }
        if (extra != null) {
            byproducts.merge(extra.asOne(), extra.amount, Double::plus)
        }

        for (recipeOutput in recipe.results) {
            if (recipeOutput.matchesType(input)) continue
            val outputItem = when (recipeOutput) {
                is FluidOrItem.Fluid -> FluidOrItem.Fluid(
                    fluid = recipeOutput.fluid,
                    amountMillibuckets = recipeOutput.amountMillibuckets * outputMulti
                )

                is FluidOrItem.Item -> FluidOrItem.Item(
                    item = recipeOutput.item.asQuantity(recipeOutput.item.amount * outputMulti)
                )
            }
            byproducts.merge(outputItem.asOne(), outputItem.amount, Double::plus)
        }

        for (recipeInput in recipe.inputs) {
            val inputItem = when (recipeInput) {
                is RecipeInput.Fluid -> FluidOrItem.Fluid(
                    fluid = recipeInput.fluids.first(),
                    amountMillibuckets = recipeInput.amountMillibuckets * outputMulti
                )

                is RecipeInput.Item -> FluidOrItem.Item(
                    item = recipeInput.representativeItem.asQuantity(recipeInput.amount * outputMulti)
                )
            }
            calculateFor(inputItem)
        }

        calculationStack.removeLast()
    }

    private fun findRecipeFor(item: PylonItem): PylonRecipe? {
        // 1. if there's a recipe with the same key as the item, use that
        PylonRegistry.RECIPE_TYPES
            .map { it.getRecipe(item.schema.key) }
            .find { it != null && it !in blacklistedRecipes }
            ?.let { return it }

        // 2. if there's a recipe which produces *only* that item, use that
        // 3. if there's multiple recipes which produce only that item, choose the *lowest* one lexographically
        val singleOutputRecipes = PylonRegistry.RECIPE_TYPES
            .flatMap { it.recipes }
            .filter { recipe -> recipe !in blacklistedRecipes && recipe.isOutput(item.stack) && recipe.results.size == 1 }
            .sortedBy { it.key }

        if (singleOutputRecipes.isNotEmpty()) {
            return singleOutputRecipes.first()
        }

        // 4. if there's a recipe which produces the item *alongside* other things, use that
        // 5. if there's multiple recipes which produce the item alongside other things, choose the *lowest* one lexographically
        val multiOutputRecipes = PylonRegistry.RECIPE_TYPES
            .flatMap { it.recipes }
            .filter { recipe -> recipe !in blacklistedRecipes && recipe.isOutput(item.stack) && recipe.results.size > 1 }
            .sortedBy { it.key }

        if (multiOutputRecipes.isNotEmpty()) {
            return multiOutputRecipes.first()
        }

        return null
    }

    private fun findRecipeFor(fluid: PylonFluid): PylonRecipe? {
        // 1. if there's a recipe with the same key as the fluid, use that
        PylonRegistry.RECIPE_TYPES
            .map { it.getRecipe(fluid.key) }
            .find { it != null && it !in blacklistedRecipes }
            ?.let { return it }

        // 2. if there's a recipe which produces *only* that fluid, use that
        // 3. if there's multiple recipes which produce only that fluid, choose the *lowest* one lexographically
        val singleOutputRecipes = PylonRegistry.RECIPE_TYPES
            .flatMap { it.recipes }
            .filter { recipe -> recipe !in blacklistedRecipes && recipe.isOutput(fluid) && recipe.results.size == 1 }
            .sortedBy { it.key }

        if (singleOutputRecipes.isNotEmpty()) {
            return singleOutputRecipes.first()
        }

        // 4. if there's a recipe which produces the fluid *alongside* other things, use that
        // 5. if there's multiple recipes which produce the fluid alongside other things, choose the *lowest* one lexographically
        val multiOutputRecipes = PylonRegistry.RECIPE_TYPES
            .flatMap { it.recipes }
            .filter { recipe -> recipe !in blacklistedRecipes && recipe.isOutput(fluid) && recipe.results.size > 1 }
            .sortedBy { it.key }

        if (multiOutputRecipes.isNotEmpty()) {
            return multiOutputRecipes.first()
        }

        return null
    }

    companion object {

        private val baseIngredients = mutableSetOf<FluidOrItem>()

        private val baseRecipes = mutableSetOf<PylonRecipe>()

        @JvmStatic
        fun addBaseIngredient(fluid: PylonFluid) {
            baseIngredients.add(FluidOrItem.Fluid(fluid, 1.0))
        }

        @JvmStatic
        fun addBaseIngredient(item: ItemStack) {
            baseIngredients.add(FluidOrItem.Item(item.asOne()))
        }

        @JvmStatic
        fun addBaseRecipe(recipe: PylonRecipe) {
            baseRecipes.add(recipe)
        }

        @JvmStatic
        fun addBaseRecipeType(type: RecipeType<*>) {
            baseRecipes.addAll(type.recipes)
        }

        @JvmStatic
        fun calculateInputsAndByproducts(input: FluidOrItem): IngredientCalculation {
            val calculator = IngredientCalculator()
            calculator.calculateFor(input)

            fun transformEntry(entry: Map.Entry<FluidOrItem, Double>) = when (entry.key) {
                is FluidOrItem.Fluid -> FluidOrItem.of(
                    fluid = (entry.key as FluidOrItem.Fluid).fluid,
                    amountMillibuckets = entry.value
                )
                is FluidOrItem.Item -> FluidOrItem.of(
                    item = (entry.key as FluidOrItem.Item).item.asQuantity(entry.value.roundToInt())
                )
            }

            return IngredientCalculation(
                calculator.ingredients.map(::transformEntry),
                calculator.byproducts.map(::transformEntry)
            )
        }
    }
}

/**
 * Stores the result of an ingredient breakdown.
 *
 * In the case of items, the resulting amount is stored in the [ItemStack]'s amount. This may lead to illegal stack sizes,
 * so before putting the items into an inventory, make sure to split them into valid stack sizes, otherwise they will be reset.
 */
data class IngredientCalculation(val inputs: List<FluidOrItem>, val byproducts: List<FluidOrItem>)

private fun FluidOrItem.asOne(): FluidOrItem = when (this) {
    is FluidOrItem.Fluid -> this.copy(amountMillibuckets = 1.0)
    is FluidOrItem.Item -> this.copy(item = this.item.asQuantity(1))
}

private val FluidOrItem.amount: Double
    get() = when (this) {
        is FluidOrItem.Fluid -> this.amountMillibuckets
        is FluidOrItem.Item -> this.item.amount.toDouble()
    }