package io.github.pylonmc.pylon.core.recipe

import io.github.pylonmc.pylon.core.fluid.PylonFluid
import io.github.pylonmc.pylon.core.item.ItemTypeWrapper
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import org.bukkit.NamespacedKey
import kotlin.math.ceil

class IngredientCalculator private constructor() {

    private val ingredients = mutableSetOf<FluidOrItem>()
    private val byproducts = mutableSetOf<FluidOrItem>()

    private val blacklistedRecipes = mutableSetOf<NamespacedKey>()

    private val calculationStack = ArrayDeque<NamespacedKey>()

    private fun calculateFor(input: FluidOrItem) {
        var input = input
        for (byproduct in byproducts.toList()) {
            if (!byproduct.matchesType(input)) continue
            byproducts.remove(byproduct)
            when (byproduct) {
                is FluidOrItem.Fluid -> {
                    input as FluidOrItem.Fluid
                    val consumed = minOf(byproduct.amountMillibuckets, input.amountMillibuckets)
                    val remainingByproduct = byproduct.amountMillibuckets - consumed
                    val remainingInput = input.amountMillibuckets - consumed
                    if (remainingByproduct > 0) {
                        byproducts.add(byproduct.copy(amountMillibuckets = remainingByproduct))
                    }
                    if (remainingInput > 0) {
                        input = input.copy(amountMillibuckets = remainingInput)
                    } else {
                        return
                    }
                }

                is FluidOrItem.Item -> {
                    input as FluidOrItem.Item
                    val consumed = minOf(byproduct.item.amount, input.item.amount)
                    val remainingByproduct = byproduct.item.amount - consumed
                    val remainingInput = input.item.amount - consumed
                    if (remainingByproduct > 0) {
                        byproducts.add(byproduct.copy(item = byproduct.item.asQuantity(remainingByproduct)))
                    }
                    if (remainingInput > 0) {
                        input = input.copy(item = input.item.asQuantity(remainingInput))
                    } else {
                        return
                    }
                }
            }
        }
        if (input.key in baseIngredients || (input is FluidOrItem.Item && ItemTypeWrapper(input.item) is ItemTypeWrapper.Vanilla)) {
            addItem(ingredients, input)
            return
        }

        var recipe: PylonRecipe?
        do {
            recipe = when (input) {
                is FluidOrItem.Fluid -> findRecipeFor(input.fluid)
                is FluidOrItem.Item -> findRecipeFor(PylonItem.from(input.item)!!) // guaranteed to be a Pylon item because of the vanilla check above
            }

            if (recipe != null && recipe.key in calculationStack) {
                blacklistedRecipes.add(recipe.key)
            }
        } while (recipe?.key in blacklistedRecipes)

        if (recipe == null) {
            addItem(ingredients, input)
            return
        }

        val output = recipe.results.find { it.matchesType(input) }
        if (output == null) {
            addItem(ingredients, input)
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
            addItem(byproducts, extra)
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
            addItem(byproducts, outputItem)
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

    private fun addItem(set: MutableSet<FluidOrItem>, item: FluidOrItem) {
        for (existing in set.toList()) {
            if (!existing.matchesType(item)) continue
            set.remove(existing)
            set.add(
                when (existing) {
                    is FluidOrItem.Fluid -> existing.copy(amountMillibuckets = existing.amountMillibuckets + (item as FluidOrItem.Fluid).amountMillibuckets)
                    is FluidOrItem.Item -> existing.copy(item = existing.item.asQuantity(existing.item.amount + (item as FluidOrItem.Item).item.amount))
                }
            )
            return
        }
        set.add(item)
    }

    private fun findRecipeFor(item: PylonItem): PylonRecipe? {
        // 1. if there's a recipe with the same key as the item, use that
        PylonRegistry.RECIPE_TYPES
            .map { it.getRecipe(item.schema.key) }
            .find { it != null && it.key !in blacklistedRecipes }
            ?.let { return it }

        // 2. if there's a recipe which produces *only* that item, use that
        // 3. if there's multiple recipes which produce only that item, choose the *lowest* one lexographically
        var fallback: PylonRecipe? = null
        val singleOutputRecipes = PylonRegistry.RECIPE_TYPES
            .flatMap { it.recipes }
            .filter { recipe -> recipe.key !in blacklistedRecipes && recipe.isOutput(item.stack) && recipe.results.size == 1 }
            .sortedBy { it.key }

        if (singleOutputRecipes.isNotEmpty()) {
            findFluidInputOnlyRecipe(singleOutputRecipes)?.let { return it }
            fallback = singleOutputRecipes.first()
        }

        // 4. if there's a recipe which produces the item *alongside* other things, use that
        // 5. if there's multiple recipes which produce the item alongside other things, choose the *lowest* one lexographically
        val multiOutputRecipes = PylonRegistry.RECIPE_TYPES
            .flatMap { it.recipes }
            .filter { recipe -> recipe.key !in blacklistedRecipes && recipe.isOutput(item.stack) && recipe.results.size > 1 }
            .sortedBy { it.key }

        if (multiOutputRecipes.isNotEmpty()) {
            findFluidInputOnlyRecipe(multiOutputRecipes)?.let { return it }
            if (fallback == null) {
                fallback = multiOutputRecipes.first()
            }
        }

        return fallback
    }

    private fun findRecipeFor(fluid: PylonFluid): PylonRecipe? {
        var fallback: PylonRecipe? = null
        // 1. if there's a recipe with the same key as the item, use that
        val recipe = PylonRegistry.RECIPE_TYPES
            .map { it.getRecipe(fluid.key) }
            .find { it != null && it.key !in blacklistedRecipes }

        if (recipe != null) {
            if (recipe.inputs.all { it is RecipeInput.Fluid }) {
                return recipe
            }

            fallback = recipe
        }

        // 2. if there's a recipe which produces *only* that item, use that
        // 3. if there's multiple recipes which produce only that item, choose the *lowest* one lexographically
        val singleOutputRecipes = PylonRegistry.RECIPE_TYPES
            .flatMap { it.recipes }
            .filter { recipe -> recipe.key !in blacklistedRecipes && recipe.isOutput(fluid) && recipe.results.size == 1 }
            .sortedBy { it.key }

        if (singleOutputRecipes.isNotEmpty()) {
            findFluidInputOnlyRecipe(singleOutputRecipes)?.let { return it }
            if (fallback == null) fallback = singleOutputRecipes.first()
        }

        // 4. if there's a recipe which produces the item *alongside* other things, use that
        // 5. if there's multiple recipes which produce the item alongside other things, choose the *lowest* one lexographically
        val multiOutputRecipes = PylonRegistry.RECIPE_TYPES
            .flatMap { it.recipes }
            .filter { recipe -> recipe.key !in blacklistedRecipes && recipe.isOutput(fluid) && recipe.results.size > 1 }
            .sortedBy { it.key }

        if (multiOutputRecipes.isNotEmpty()) {
            findFluidInputOnlyRecipe(multiOutputRecipes)?.let { return it }
            if (fallback == null) {
                fallback = multiOutputRecipes.first()
            }
        }

        return fallback
    }

    companion object {

        private val baseIngredients = mutableSetOf<NamespacedKey>()

        @JvmStatic
        fun addBaseIngredient(key: NamespacedKey) {
            baseIngredients.add(key)
        }

        @JvmStatic
        fun calculateInputsAndByproducts(input: FluidOrItem): IngredientCalculation {
            val calculator = IngredientCalculator()
            calculator.calculateFor(input)
            return IngredientCalculation(calculator.ingredients, calculator.byproducts)
        }
    }
}

data class IngredientCalculation(val inputs: Set<FluidOrItem>, val byproducts: Set<FluidOrItem>)

private class RecursiveRecipeException : Exception()

private fun findFluidInputOnlyRecipe(list: List<PylonRecipe>): PylonRecipe? =
    list.firstOrNull { it.inputs.all { input -> input is RecipeInput.Fluid } }