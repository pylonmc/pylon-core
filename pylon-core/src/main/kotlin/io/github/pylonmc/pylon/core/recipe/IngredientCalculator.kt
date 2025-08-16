@file:JvmName("IngredientCalculator")

package io.github.pylonmc.pylon.core.recipe

import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.util.findRecipeFor
import io.github.pylonmc.pylon.core.util.isPylonSimilar
import org.bukkit.inventory.ItemStack
import kotlin.math.ceil

class IngredientCalculator {
    companion object {
        @JvmStatic
        fun calculate(stack: ItemStack): IngredientCalculation {
            val pylonItem = PylonItem.fromStack(stack)
            return pylonItem?.let {
                val requiredAmount = stack.amount
                val baseCalculation = calculateWithOutputAmount(it)
                val multiplier = ceil(requiredAmount.toDouble() / baseCalculation.outputAmount)
                (baseCalculation * multiplier).copy(outputAmount = requiredAmount)
            } ?: IngredientCalculation.asIngredient(stack)
        }

        private fun calculateWithOutputAmount(pylonItem: PylonItem): IngredientCalculation {
            val baseResult = IngredientCalculation.empty()
            val recipe = findRecipeFor(pylonItem) ?: return baseResult.copy(outputAmount = 1)

            var recipeOutputAmount = 0
            recipe.results.forEach { outputResult ->
                if (outputResult is FluidOrItem.Item &&
                    outputResult.item.isPylonSimilar(pylonItem.stack)
                ) {
                    recipeOutputAmount += outputResult.item.amount
                }
            }
            baseResult.outputAmount = if (recipeOutputAmount > 0) recipeOutputAmount else 1

            recipe.inputs.forEach { inputComponent ->
                when (inputComponent) {
                    is FluidOrItem.Item -> {
                        val subCalculation = calculate(inputComponent.item)
                        baseResult += subCalculation
                    }

                    is FluidOrItem.Fluid -> baseResult.inputs += inputComponent
                }
            }

            recipe.results.forEach { outputResult ->
                when (outputResult) {
                    is FluidOrItem.Item -> {
                        if (!outputResult.item.isPylonSimilar(pylonItem.stack)) {
                            baseResult.alongProducts += outputResult
                        }
                    }

                    is FluidOrItem.Fluid -> baseResult.alongProducts += outputResult
                }
            }

            return baseResult
        }

        @JvmStatic
        fun calculate(pylonItem: PylonItem): IngredientCalculation {
            return calculateWithOutputAmount(pylonItem)
        }

        @JvmStatic
        fun calculate(fluid: FluidOrItem.Fluid): IngredientCalculation {
            val recipe = findRecipeFor(fluid.fluid)
                ?: return IngredientCalculation(
                    inputs = mutableListOf(fluid),
                    alongProducts = mutableListOf(),
                    outputAmount = fluid.amountMillibuckets.toInt()
                )

            val targetFluid = recipe.results
                .find { it is FluidOrItem.Fluid && it.fluid == fluid.fluid } as? FluidOrItem.Fluid
                ?: return IngredientCalculation(
                    inputs = mutableListOf(fluid),
                    alongProducts = mutableListOf(),
                    outputAmount = fluid.amountMillibuckets.toInt()
                )

            val additionalAlongProducts = recipe.results.filter { it != targetFluid }
            val quantityMultiplier = ceil(fluid.amountMillibuckets / targetFluid.amountMillibuckets)

            return (IngredientCalculation(
                recipe.inputs,
                additionalAlongProducts,
                targetFluid.amountMillibuckets.toInt()
            ) * quantityMultiplier)
                .copy(outputAmount = fluid.amountMillibuckets.toInt())
        }
    }

    data class IngredientCalculation(
        val inputs: MutableList<FluidOrItem>,
        val alongProducts: MutableList<FluidOrItem>,
        var outputAmount: Int
    ) {
        constructor(inputs: List<FluidOrItem>, alongProducts: List<FluidOrItem>, outputAmount: Int) :
                this(inputs.toMutableList(), alongProducts.toMutableList(), outputAmount)

        @JvmName("add")
        operator fun plusAssign(other: IngredientCalculation) {
            this.inputs.addAll(other.inputs)
            this.alongProducts.addAll(other.alongProducts)
        }

        @JvmName("multiply")
        operator fun times(multiplier: Double): IngredientCalculation {
            if (multiplier <= 0.0) return this

            val scaledInputs = inputs.map { scaleComponent(it, multiplier) }
            val scaledAlongProducts = alongProducts.map { scaleComponent(it, multiplier) }
            val scaledOutputAmount = ceil(outputAmount * multiplier).toInt()
            return IngredientCalculation(scaledInputs, scaledAlongProducts, scaledOutputAmount)
        }

        private fun scaleComponent(component: FluidOrItem, multiplier: Double): FluidOrItem {
            return when (component) {
                is FluidOrItem.Fluid ->
                    FluidOrItem.of(component.fluid, component.amountMillibuckets * multiplier)

                is FluidOrItem.Item -> {
                    val newAmount = ceil(component.item.amount * multiplier).toInt()
                    val newItem = component.item.clone().apply { amount = newAmount }
                    FluidOrItem.of(newItem)
                }
            }
        }

        companion object {
            @JvmStatic
            fun empty(): IngredientCalculation {
                return IngredientCalculation(emptyList(), emptyList(), 0)
            }

            @JvmStatic
            fun asIngredient(stack: ItemStack): IngredientCalculation {
                return IngredientCalculation(
                    inputs = listOf(FluidOrItem.of(stack)),
                    alongProducts = emptyList(),
                    outputAmount = stack.amount
                )
            }
        }
    }
}
    