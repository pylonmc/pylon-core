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
            return pylonItem?.let { calculate(it) }
                ?: IngredientCalculation.asIngredient(stack)
        }

        @JvmStatic
        fun calculate(pylonItem: PylonItem): IngredientCalculation {
            val res = IngredientCalculation.empty()

            val recipe = findRecipeFor(pylonItem)
                ?: return res

            recipe.inputs.forEach { inputComponent ->
                when (inputComponent) {
                    is FluidOrItem.Item -> {
                        val subCalculation = calculate(inputComponent.item)
                        res.inputs += subCalculation.inputs
                        res.alongProducts += subCalculation.alongProducts
                    }
                    is FluidOrItem.Fluid -> res.inputs += inputComponent
                }
            }

            recipe.results.forEach { outputResult ->
                when (outputResult) {
                    is FluidOrItem.Item -> {
                        if (!outputResult.item.isPylonSimilar(pylonItem.stack)) {
                            res.alongProducts += outputResult
                        }
                    }
                    is FluidOrItem.Fluid -> res.alongProducts += outputResult
                }
            }

            return res
        }

        @JvmStatic
        fun calculate(fluid: FluidOrItem.Fluid): IngredientCalculation {
            val recipe = findRecipeFor(fluid.fluid)
                ?: return IngredientCalculation(listOf(fluid), emptyList())

            val targetFluid = recipe.results
                .find { it is FluidOrItem.Fluid && it.fluid == fluid.fluid } as? FluidOrItem.Fluid
                ?: return IngredientCalculation(listOf(fluid), emptyList())

            val additionalAlongProducts = recipe.results.filter { it != targetFluid }
            val quantityMultiplier = ceil(fluid.amountMillibuckets / targetFluid.amountMillibuckets)

            return IngredientCalculation(recipe.inputs, additionalAlongProducts) * quantityMultiplier
        }
    }

    data class IngredientCalculation(
        val inputs: MutableList<FluidOrItem>,
        val alongProducts: MutableList<FluidOrItem>
    ) {
        constructor(inputs: List<FluidOrItem>, alongProducts: List<FluidOrItem>) :
                this(inputs.toMutableList(), alongProducts.toMutableList())

        @JvmName("plus")
        operator fun plusAssign(other: IngredientCalculation) {
            this.inputs.addAll(other.inputs)
            this.alongProducts.addAll(other.alongProducts)
        }

        @JvmName("multiply")
        operator fun times(multiplier: Double): IngredientCalculation {
            val scaledInputs = inputs.map { component ->
                when (component) {
                    is FluidOrItem.Fluid ->
                        FluidOrItem.of(component.fluid, component.amountMillibuckets * multiplier)

                    is FluidOrItem.Item -> {
                        val newItem = component.item.clone().apply {
                            amount *= ceil(multiplier).toInt()
                        }
                        FluidOrItem.of(newItem)
                    }
                }
            }
            return IngredientCalculation(scaledInputs, alongProducts.toList())
        }

        companion object {
            @JvmStatic
            fun asIngredient(stack: ItemStack): IngredientCalculation {
                return IngredientCalculation(listOf(FluidOrItem.of(stack)), emptyList())
            }

            @JvmStatic
            fun empty(): IngredientCalculation {
                return IngredientCalculation(emptyList(), emptyList())
            }
        }
    }
}
