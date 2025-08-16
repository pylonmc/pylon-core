@file:JvmName("IngredientCalculator")

package io.github.pylonmc.pylon.core.recipe

import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.recipe.IngredientCalculator.Companion.calculateBase
import io.github.pylonmc.pylon.core.recipe.IngredientCalculator.Companion.calculateFinal
import io.github.pylonmc.pylon.core.util.findRecipeFor
import io.github.pylonmc.pylon.core.util.isPylonSimilar
import org.bukkit.inventory.ItemStack
import kotlin.math.ceil

/**
 * Used to recursively calculate raw material requirements and
 * along products of items/fluids, supporting quantity scaling
 *
 * The two `calculateFinal` methods are the main entry points,
 * It's supposed to only call them from here.
 *
 * How it works:
 *
 *                    Vanilla items (Not support yet)
 *  [calculateFinal] ----------------> [IngredientCalculation.asIngredient]
 *        |
 *        | PylonItems/PylonFluid
 *        ↓
 *  [calculateBase] ---------------------------------------> [Find recipe] <----------------------╮
 *  (calculate out all the inputs, along products)                |                               |
 *  (and the final main product's amount)                         |                               |
 *  (see `baseCalculation`)                                       ↓                               |
 *        |   ←------------------------------------------╮   [Filter inputs and along products]   |
 *        | Finally calculate the actual inputs' amount  |        |                               |
 *        | For example:                                 |        |                               |
 *        | ```                                          |        ↓                               |
 *        | stack.amount = 11 (the arg)                  |   [Try to find ingredients' recipe] ---╯
 *        | baseCalculation = {                          |        |
 *        |     inputs        = [Ax1, Bx13, Cx19]        |        |
 *        |     alongProducts = [Mx16, Nx9]              |        ↓
 *        |     outputAmount  = 3                        ╰-- [Merge ingredients and return value]
 *        | }
 *        | ```
 *        | After scaling (×4), it will be:
 *        | baseCalculation = {
 *        |     inputs        = [Ax4, Bx52, Cx76]
 *        |     alongProducts = [Mx64, Nx36]
 *        |     outputAmount  = 12
 *        | }
 *        |
 *        ↓
 *  [scaling and return value]
 *
 * @author balugaq
 */
class IngredientCalculator {
    companion object {
        /**
         * Calculate the final material requirements for an item stack with quantity (including quantity scaling)
         * @param stack Target item stack (including quantity information to be calculated)
         * @return Calculation result including raw materials, along products and target output quantity
         */
        @JvmStatic
        fun calculateFinal(stack: ItemStack): IngredientCalculation {
            val pylonItem = PylonItem.fromStack(stack)
            return pylonItem?.let {
                // PylonItem has recipes
                val requiredAmount = stack.amount.toDouble()
                val baseCalculation = calculateBase(it)
                val recipeOutputAmount = baseCalculation.outputAmount
                val scaleMultiplier = ceil(requiredAmount / recipeOutputAmount)
                baseCalculation.scaleBy(scaleMultiplier).copy(outputAmount = requiredAmount)
            } ?: IngredientCalculation.asIngredient(stack) // Not PylonItem, no recipes for vanilla items so far.
        }

        /**
         * Calculate material requirements and along products for fluids
         * @param fluid Target fluid (including quantity information to be calculated)
         * @return Calculation result including raw materials, along products and target fluid output quantity
         */
        @JvmStatic
        fun calculateFinal(fluid: FluidOrItem.Fluid): IngredientCalculation {
            val recipe = findRecipeFor(fluid.fluid)
                ?: return IngredientCalculation(
                    inputs = mutableListOf(fluid),
                    alongProducts = mutableListOf(),
                    outputAmount = fluid.amountMillibuckets
                )

            val targetFluid = recipe.results
                .find { it is FluidOrItem.Fluid && it.fluid == fluid.fluid } as? FluidOrItem.Fluid
                ?: return IngredientCalculation(
                    inputs = mutableListOf(fluid),
                    alongProducts = mutableListOf(),
                    outputAmount = fluid.amountMillibuckets
                )

            val additionalAlongProducts = recipe.results.filter { it != targetFluid }
            val scaleMultiplier = ceil(fluid.amountMillibuckets / targetFluid.amountMillibuckets)

            return IngredientCalculation(
                inputs = recipe.inputs.toMutableList(),
                alongProducts = additionalAlongProducts.toMutableList(),
                outputAmount = targetFluid.amountMillibuckets
            ).scaleBy(scaleMultiplier)
                .copy(outputAmount = fluid.amountMillibuckets)
        }

        /**
         * Calculate the basic recipe data of a single item (for recursive calculation)
         * @param pylonItem Target item (without quantity scaling, only calculate single recipe)
         * @return Basic recipe raw materials, along products and single output quantity
         */
        @JvmStatic
        fun calculateBase(pylonItem: PylonItem): IngredientCalculation {
            val baseResult = IngredientCalculation.empty()
            val recipe = findRecipeFor(pylonItem) ?: return baseResult.copy(outputAmount = 1.toDouble())

            // Calculate the main product quantity output by the recipe per cycle
            val recipeOutputAmount = getRecipeOutputAmount(recipe, pylonItem)
            baseResult.outputAmount = recipeOutputAmount

            recipe.inputs.forEach { fluidOrItem ->
                when (fluidOrItem) {
                    is FluidOrItem.Item -> {
                        val subCalculation = calculateFinal(fluidOrItem.item)
                        baseResult.mergeSubCalculation(subCalculation)
                    }

                    is FluidOrItem.Fluid -> {
                        val subCalculation = calculateFinal(fluidOrItem)
                        baseResult.mergeSubCalculation(subCalculation)
                    }
                }
            }

            // exclude main product, but including along products
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
        fun calculateBase(itemStack: ItemStack): IngredientCalculation {
            val pylonItem = PylonItem.fromStack(itemStack)
            return pylonItem?.let { calculateBase(it) }
                ?: IngredientCalculation.asIngredient(itemStack)
        }

        /**
         * Calculate the single output quantity of the main product in the recipe separately
         * @param recipe Target recipe
         * @param targetItem Main product item
         * @return Total quantity of main product output per recipe cycle
         */
        @JvmStatic
        fun getRecipeOutputAmount(recipe: PylonRecipe, targetItem: PylonItem): Double {
            var outputAmount = 0
            recipe.results.forEach { outputResult ->
                if (outputResult is FluidOrItem.Item &&
                    outputResult.item.isPylonSimilar(targetItem.stack)
                ) {
                    outputAmount += outputResult.item.amount
                }
            }
            // Fallback: If the recipe does not explicitly specify the main product, default to 1 output per cycle
            return (if (outputAmount > 0) outputAmount else 1).toDouble()
        }
    }

    /**
     * @author balugaq
     */
    data class IngredientCalculation(
        val inputs: MutableList<FluidOrItem>,
        val alongProducts: MutableList<FluidOrItem>,
        /**
         * Main product output quantity:
         * - In basic recipe calculation  -> the quantity of main product output per recipe cycle
         * - In final calculation results -> the total quantity of main product to be produced (after scaling)
         */
        var outputAmount: Double
    ) {
        constructor(inputs: List<FluidOrItem>, alongProducts: List<FluidOrItem>, outputAmount: Double) :
                this(inputs.toMutableList(), alongProducts.toMutableList(), outputAmount)

        /**
         * Merge sub-recipe calculation results (only merge raw materials and along products,
         * without affecting main product quantity)
         * Used in recursive calculation to integrate inputs and along products from sub-recipes
         */
        fun mergeSubCalculation(other: IngredientCalculation) {
            this.inputs += other.inputs
            this.alongProducts += other.alongProducts
        }

        /**
         * Scale raw materials, along products and main product quantities by multiplier
         * @param multiplier Scaling multiplier (must be greater than 0)
         * @return Scaled calculation result
         */
        fun scaleBy(multiplier: Double): IngredientCalculation {
            if (multiplier <= 0.0) return this

            val scaledInputs = inputs.map { scaleComponent(it, multiplier) }
            val scaledAlongProducts = alongProducts.map { scaleComponent(it, multiplier) }
            val scaledOutputAmount = ceil(outputAmount * multiplier)

            return IngredientCalculation(
                scaledInputs.toMutableList(),
                scaledAlongProducts.toMutableList(),
                scaledOutputAmount
            )
        }

        /**
         * Scale the quantity of a single component (item or fluid)
         * @param component Component to be scaled
         * @param multiplier Scaling multiplier
         * @return Scaled component
         */
        internal fun scaleComponent(component: FluidOrItem, multiplier: Double): FluidOrItem {
            return when (component) {
                is FluidOrItem.Fluid ->
                    FluidOrItem.of(component.fluid, component.amountMillibuckets * multiplier)

                is FluidOrItem.Item -> {
                    val newAmount = ceil(component.item.amount * multiplier)
                    val newItem = component.item.clone().apply { amount = newAmount.toInt() }
                    FluidOrItem.of(newItem)
                }
            }
        }

        companion object {
            /**
             * Create an empty calculation result (empty raw materials and along products, output quantity is 0)
             */
            @JvmStatic
            fun empty(): IngredientCalculation {
                return IngredientCalculation(emptyList(), emptyList(), 0.toDouble())
            }

            /**
             * Directly convert an item stack to a basic raw material (for items without recipes)
             * @param stack Target item stack
             * @return Calculation result with this item as raw material
             */
            @JvmStatic
            fun asIngredient(stack: ItemStack): IngredientCalculation {
                return IngredientCalculation(
                    inputs = mutableListOf(FluidOrItem.of(stack)),
                    alongProducts = mutableListOf(),
                    outputAmount = stack.amount.toDouble()
                )
            }
        }
    }
}