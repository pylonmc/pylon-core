package io.github.pylonmc.pylon.core.recipe

import io.github.pylonmc.pylon.core.fluid.PylonFluid
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.recipe.IngredientCalculator.calculateBase
import io.github.pylonmc.pylon.core.recipe.IngredientCalculator.calculateFinal
import io.github.pylonmc.pylon.core.recipe.IngredientCalculator.checkRecursiveDepth
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import net.kyori.adventure.key.Key
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice

/**
 * Used to recursively calculate raw material requirements and
 * along products of items/fluids, supporting amount scaling
 *
 * The two `calculateFinal` methods are the main entry points,
 * It's supposed to only call them from here.
 *
 * How it works:
 *
 *                    Vanilla items (Not support yet)
 *  [calculateFinal] --------------------------------------> [IngredientCalculation.asIngredient]
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
 *        |     intermediates = [Mx16, Nx9]              |        ↓
 *        |     outputAmount  = 3                        ╰-- [Merge ingredients and return value]
 *        | }
 *        | ```
 *        |
 *        | After scaling (×4), it will be:
 *        | ```
 *        | baseCalculation = {
 *        |     inputs        = [Ax4, Bx52, Cx76]
 *        |     intermediates = [Mx64, Nx36]
 *        |     outputAmount  = 12
 *        | }
 *        | ```
 *        |
 *        ↓
 *  [scaling and return value]
 *
 * @author balugaq
 */
object IngredientCalculator {
    /**
     * The maximum recursive depth allowed
     *
     * @see checkRecursiveDepth
     */
    @JvmSynthetic
    internal const val RECURSIVE_THRESHOLD = 100

    /**
     * Calculate the final material requirements for an item stack with amount (including amount scaling)
     * @param stack Target item stack (including amount information to be calculated)
     * @return Calculation result including raw materials, along products and target output amount
     */
    @JvmStatic
    fun calculateFinal(stack: ItemStack, depth: Int = 1): IngredientCalculation {
        checkRecursiveDepth(depth)

        val pylonItem = PylonItem.fromStack(stack)
        if (pylonItem != null) {
            // PylonItem has recipes
            val requiredAmount = stack.amount.toDouble()
            val baseCalculation = calculateBase(pylonItem, depth + 1)
            val recipeOutputAmount = baseCalculation.outputAmount
            val scaleMultiplier = requiredAmount / recipeOutputAmount
            return baseCalculation.scaleBy(scaleMultiplier).copy(outputAmount = requiredAmount)
        }

        val pylonFluid = PylonFluid.fromStack(stack)
        if (pylonFluid != null) {
            // PylonFluid has recipes
            val requiredAmount = stack.amount.toDouble()
            val baseCalculation = calculateFinal(FluidOrItem.of(pylonFluid, requiredAmount) as FluidOrItem.Fluid, depth + 1)
            val recipeOutputAmount = baseCalculation.outputAmount
            val scaleMultiplier = requiredAmount / recipeOutputAmount
            return baseCalculation.scaleBy(scaleMultiplier).copy(outputAmount = requiredAmount)
        }

        return IngredientCalculation.asIngredient(stack) // Not PylonItem, no recipes for vanilla items so far.
    }

    /**
     * Calculate material requirements and along products for fluids
     * @param fluid Target fluid (including amount information to be calculated)
     * @return Calculation result including raw materials, along products and target fluid output amount
     */
    @JvmStatic
    fun calculateFinal(fluid: FluidOrItem.Fluid, depth: Int = 1): IngredientCalculation {
        checkRecursiveDepth(depth)

        val recipe = findRecipeFor(fluid.fluid)
            ?: return IngredientCalculation(
                inputs = mutableListOf(Container.of(fluid)),
                intermediates = mutableListOf(),
                outputAmount = fluid.amountMillibuckets
            )

        val targetFluid = recipe.results
            .find { it is FluidOrItem.Fluid && it.fluid == fluid.fluid } as? FluidOrItem.Fluid
            ?: return IngredientCalculation(
                inputs = Container.of(mutableListOf(fluid)),
                intermediates = mutableListOf(),
                outputAmount = fluid.amountMillibuckets
            )

        val additionalIntermediates = recipe.results.filter { it != targetFluid }
        val scaleMultiplier = fluid.amountMillibuckets / targetFluid.amountMillibuckets

        return IngredientCalculation(
            inputs = Container.of(recipe.inputs.mapTo(mutableListOf()) {
                when (it) {
                    is RecipeInput.Item -> FluidOrItem.Item(it.representativeItem)
                    is RecipeInput.Fluid -> FluidOrItem.Fluid(it.fluids.first(), it.amountMillibuckets)
                }
            }),
            intermediates = Container.of(additionalIntermediates.toMutableList()),
            outputAmount = targetFluid.amountMillibuckets
        ).scaleBy(scaleMultiplier)
            .copy(outputAmount = fluid.amountMillibuckets)
    }

    /**
     * Calculate the basic recipe data of a single item (for *recursive* calculation)
     * @param pylonItem Target item (without amount scaling, only calculate single recipe)
     * @return Basic recipe raw materials, along products and single output amount
     */
    @JvmStatic
    fun calculateBase(pylonItem: PylonItem, depth: Int = 1): IngredientCalculation {
        checkRecursiveDepth(depth)

        val baseResult = IngredientCalculation.empty()
        val rawRecipe = findRecipeFor(pylonItem) ?: return baseResult.copy(outputAmount = 1.toDouble())
        val recipe = copyRecipe(rawRecipe)

        // Calculate the main product amount output by the recipe per cycle
        val recipeOutputAmount = getRecipeOutputAmount(recipe, pylonItem)
        baseResult.outputAmount = recipeOutputAmount

        for (input in recipe.inputs) {
            when (input) {
                is RecipeInput.Item -> {
                    val subCalculation = calculateFinal(input.representativeItem, depth + 1)
                    baseResult.mergeSubCalculation(subCalculation)
                }

                is RecipeInput.Fluid -> {
                    val subCalculation = calculateFinal(FluidOrItem.Fluid(input.fluids.first(), input.amountMillibuckets), depth + 1)
                    baseResult.mergeSubCalculation(subCalculation)
                }
            }
        }

        // exclude main product, but including intermediates
        for (outputResult in recipe.results) {
            when (outputResult) {
                is FluidOrItem.Item -> {
                    if (!outputResult.item.isSimilar(pylonItem.stack)) {
                        baseResult.intermediates += Container.of(outputResult)
                    }
                }

                is FluidOrItem.Fluid -> baseResult.intermediates += Container.of(outputResult)
            }
        }

        return baseResult
    }

    /**
     * Calculate the single output amount of the main product in the recipe separately
     * @param recipe Target recipe
     * @param targetItem Main product item
     * @return Total amount of main product output per recipe cycle
     */
    @JvmStatic
    fun getRecipeOutputAmount(recipe: PylonRecipe, targetItem: PylonItem): Double {
        var outputAmount = 0
        recipe.results.forEach { outputResult ->
            if (outputResult is FluidOrItem.Item &&
                outputResult.item.isSimilar(targetItem.stack)
            ) {
                outputAmount += outputResult.item.amount
            }
        }
        // Fallback: If the recipe does not explicitly specify the main product, default to 1 output per cycle
        return if (outputAmount > 0) outputAmount.toDouble() else 1.0
    }

    @JvmSynthetic
    internal fun checkRecursiveDepth(depth: Int) {
        if (depth > RECURSIVE_THRESHOLD) {
            throw RuntimeException("Recursive depth exceeded the threshold of $RECURSIVE_THRESHOLD")
        }
    }

    @JvmSynthetic
    internal fun copyRecipe(recipe: PylonRecipe): PylonRecipe {
        return InternalRecipe(recipe)
    }
}

/**
 * @author balugaq
 */
data class IngredientCalculation(
    val inputs: MutableList<Container>,
    val intermediates: MutableList<Container>,
    /**
     * Output amount:
     * - In basic recipe calculation  -> the amount of main product output per recipe cycle
     * - In final calculation results -> the total amount of main product to be produced (after scaling)
     */
    var outputAmount: Double
) {
    /**
     * Merge sub-recipe calculation results (only merge raw materials and along products,
     * without affecting main product amount)
     * Used in recursive calculation to integrate inputs and along products from sub-recipes
     */
    fun mergeSubCalculation(other: IngredientCalculation) {
        this.inputs += other.inputs
        this.intermediates += other.intermediates
    }

    /**
     * Scale raw materials, along products and main product amounts by multiplier
     * @param multiplier Scaling multiplier (must be greater than 0)
     * @return Scaled calculation result
     */
    fun scaleBy(multiplier: Double): IngredientCalculation {
        if (multiplier <= 0.0) return this

        val scaledInputs = inputs.map { scaleComponent(it, multiplier) }
        val scaledIntermediates = intermediates.map { scaleComponent(it, multiplier) }
        val scaledOutputAmount = outputAmount * multiplier

        return IngredientCalculation(
            scaledInputs.toMutableList(),
            scaledIntermediates.toMutableList(),
            scaledOutputAmount
        )
    }

    /**
     * Merge all the similar components and return a clone
     */
    fun flat(): IngredientCalculation {
        val flattedInput = mutableListOf<Container>()
        flat(inputs, flattedInput)

        val flattedIntermediates = mutableListOf<Container>()
        flat(intermediates, flattedIntermediates)

        return IngredientCalculation(
            flattedInput.toMutableList(),
            flattedIntermediates.toMutableList(),
            outputAmount
        )
    }

    /**
     * Scale the amount of a single component (item or fluid)
     * @param component Component to be scaled
     * @param multiplier Scaling multiplier
     * @return Scaled component
     */
    private fun scaleComponent(component: Container, multiplier: Double): Container {
        return when (component) {
            is Container.Fluid ->
                Container.of(component.fluid, component.amountMillibuckets * multiplier)

            is Container.Item -> {
                val newAmount = component.item.amount * multiplier
                val newItem = component.item.clone().apply { amount = newAmount.toInt() }
                Container.of(newItem)
            }
        }
    }

    companion object {
        /**
         * Create an empty calculation result (empty raw materials and along products, output amount is 0)
         */
        @JvmStatic
        fun empty(): IngredientCalculation {
            return IngredientCalculation(mutableListOf(), mutableListOf(), 0.toDouble())
        }

        /**
         * Directly convert an item stack to a basic raw material (for items without recipes)
         * @param stack Target item stack
         * @return Calculation result with this item as raw material
         */
        @JvmStatic
        fun asIngredient(stack: ItemStack): IngredientCalculation {
            return IngredientCalculation(
                inputs = mutableListOf(Container.of(stack)),
                intermediates = mutableListOf(),
                outputAmount = stack.amount.toDouble()
            )
        }
    }
}

/**
 * For internal use only (Copy recipe)
 *
 * @author balugaq
 */
internal class InternalRecipe(private val recipe: PylonRecipe) : PylonRecipe by recipe {
    override val inputs: List<RecipeInput>
        get() = recipe.inputs.map {
            when (it) {
                is RecipeInput.Fluid -> RecipeInput.Fluid(it.fluids.toMutableSet(), it.amountMillibuckets)
                is RecipeInput.Item -> RecipeInput.Item(it.items.toMutableSet(), it.amount)
            }
        }

    override val results: List<FluidOrItem>
        get() = recipe.results.map {
            when (it) {
                is FluidOrItem.Fluid -> FluidOrItem.of(it.fluid, it.amountMillibuckets)
                is FluidOrItem.Item -> FluidOrItem.of(it.item.clone())
            }
        }

    override fun key(): Key {
        return recipe.key()
    }
}

/**
 * A variable FluidOrItem container
 *
 * @author balugaq
 */
sealed class Container {
    data class Item(val item: ItemStack) : Container()
    data class Fluid(val fluid: PylonFluid, var amountMillibuckets: Double) : Container()

    fun isSimilar(other: Container): Boolean {
        return when (this) {
            is Item -> when (other) {
                is Item -> item.isSimilar(other.item)
                is Fluid -> false
            }

            is Fluid -> when (other) {
                is Item -> false
                is Fluid -> fluid == other.fluid
            }
        }
    }

    companion object {
        fun of(fluidOrItem: FluidOrItem): Container {
            return when (fluidOrItem) {
                is FluidOrItem.Fluid -> Fluid(fluidOrItem.fluid, fluidOrItem.amountMillibuckets)
                is FluidOrItem.Item -> Item(fluidOrItem.item.clone())
            }
        }

        fun of(fluid: PylonFluid, amountMillibuckets: Double): Container {
            return Fluid(fluid, amountMillibuckets)
        }

        fun of(item: ItemStack): Container {
            return Item(item.clone())
        }

        fun of(item: ItemStack, amount: Int): Container {
            return Item(item.asQuantity(amount))
        }

        fun of(choice: RecipeChoice): Container {
            return Item(choice.itemStack.clone())
        }

        fun of(list: List<FluidOrItem>): MutableList<Container> {
            return list.map { of(it) }.toMutableList()
        }
    }
}

private fun findRecipeFor(item: PylonItem): PylonRecipe? {
    // 1. if there's a recipe with the same key as the item, use that
    PylonRegistry.RECIPE_TYPES
        .map { it.getRecipe(item.schema.key) }
        .find { it != null }?.let { return it }

    // 2. if there's a recipe which produces *only* that item, use that
    // 3. if there's multiple recipes which produce only that item, choose the *lowest* one lexographically
    var fallback: PylonRecipe? = null
    val singleOutputRecipes = PylonRegistry.RECIPE_TYPES.asSequence()
        .flatMap { it.recipes }
        .filter { recipe -> recipe.isOutput(item.stack) && recipe.results.size == 1 }
        .sortedBy { it.key }
        .toList()

    if (singleOutputRecipes.isNotEmpty()) {
        findFluidInputOnlyRecipe(singleOutputRecipes)?.let { return it }
        fallback = singleOutputRecipes.first()
    }

    // 4. if there's a recipe which produces the item *alongside* other things, use that
    // 5. if there's multiple recipes which produce the item alongside other things, choose the *lowest* one lexographically
    val multiOutputRecipes = PylonRegistry.RECIPE_TYPES.asSequence()
        .flatMap { it.recipes }
        .filter { recipe -> recipe.isOutput(item.stack) && recipe.results.size > 1 }
        .sortedBy { it.key }
        .toList()

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
        .find { it != null }

    if (recipe != null) {
        if (recipe.inputs.all { input ->
            when (input) {
                is RecipeInput.Fluid -> true
                is RecipeInput.Item -> false
            }
        }) {
            return recipe
        }

        fallback = recipe
    }

    // 2. if there's a recipe which produces *only* that item, use that
    // 3. if there's multiple recipes which produce only that item, choose the *lowest* one lexographically
    val singleOutputRecipes = PylonRegistry.RECIPE_TYPES.asSequence()
        .flatMap { it.recipes }
        .filter { recipe -> recipe.isOutput(fluid) && recipe.results.size == 1 }
        .sortedBy { it.key }
        .toList()

    if (singleOutputRecipes.isNotEmpty()) {
        findFluidInputOnlyRecipe(singleOutputRecipes)?.let { return it }
        if (fallback == null) fallback = singleOutputRecipes.first()
    }

    // 4. if there's a recipe which produces the item *alongside* other things, use that
    // 5. if there's multiple recipes which produce the item alongside other things, choose the *lowest* one lexographically
    val multiOutputRecipes = PylonRegistry.RECIPE_TYPES.asSequence()
        .flatMap { it.recipes }
        .filter { recipe -> recipe.isOutput(fluid) && recipe.results.size > 1 }
        .sortedBy { it.key }
        .toList()

    if (multiOutputRecipes.isNotEmpty()) {
        findFluidInputOnlyRecipe(multiOutputRecipes)?.let { return it }
        if (fallback == null) {
            fallback = multiOutputRecipes.first()
        }
    }

    return fallback
}

private fun findFluidInputOnlyRecipe(list: List<PylonRecipe>): PylonRecipe? =
    list.firstOrNull {
        it.inputs.all { input ->
            when (input) {
                is RecipeInput.Fluid -> true
                is RecipeInput.Item -> false
            }
        }
    }

private fun flat(from: MutableList<Container>, to: MutableList<Container>) {
    for (component in from) {
        var isNewObject = true
        for (exist in to) {
            if (exist.isSimilar(component)) {
                if (exist is Container.Item && component is Container.Item) {
                    exist.item.amount += component.item.amount
                    isNewObject = false
                } else if (exist is Container.Fluid && component is Container.Fluid) {
                    exist.amountMillibuckets += component.amountMillibuckets
                    isNewObject = false
                }
            }
        }
        if (isNewObject) {
            to += component
        }
    }
}