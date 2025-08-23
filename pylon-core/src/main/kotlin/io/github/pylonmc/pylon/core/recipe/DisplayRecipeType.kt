package io.github.pylonmc.pylon.core.recipe

import io.github.pylonmc.pylon.core.util.pylonKey

/**
 * A [RecipeType] to be added to when you wish to display a recipe but nothing else.
 * For example, a hypothetical water pump may use this to display "I can get water from a source block"
 * but not actually have a whole separate recipe type with such unneeded stuff as "code" and "logic".
 */
object DisplayRecipeType : RecipeType<PylonRecipe>(pylonKey("display"))