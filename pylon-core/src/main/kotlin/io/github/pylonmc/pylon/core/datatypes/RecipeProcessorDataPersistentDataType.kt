package io.github.pylonmc.pylon.core.datatypes

import io.github.pylonmc.pylon.core.block.base.PylonRecipeProcessor
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.pylonKey
import io.github.pylonmc.pylon.core.util.setNullable
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

internal object RecipeProcessorDataPersistentDataType : PersistentDataType<PersistentDataContainer, PylonRecipeProcessor.RecipeProcessorData> {

    private val RECIPE_TYPE_KEY = pylonKey("recipe_type")
    private val CURRENT_RECIPE_KEY = pylonKey("current_recipe")
    private val TOTAL_RECIPE_TICKS_KEY = pylonKey("total_recipe_ticks")
    private val RECIPE_TICKS_REMAINING_KEY = pylonKey("recipe_ticks_remaining")

    private val RECIPE_TYPE_TYPE = PylonSerializers.KEYED.keyedTypeFrom { key -> PylonRegistry.RECIPE_TYPES.getOrThrow(key) }

    override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java

    override fun getComplexType(): Class<PylonRecipeProcessor.RecipeProcessorData> = PylonRecipeProcessor.RecipeProcessorData::class.java

    override fun fromPrimitive(primitive: PersistentDataContainer, context: PersistentDataAdapterContext): PylonRecipeProcessor.RecipeProcessorData {
        val recipeType = primitive.get(RECIPE_TYPE_KEY, RECIPE_TYPE_TYPE)!!
        val recipePDT = PylonSerializers.KEYED.keyedTypeFrom { recipeType.getRecipeOrThrow(it) }
        return PylonRecipeProcessor.RecipeProcessorData(
            recipeType,
            primitive.get(CURRENT_RECIPE_KEY, recipePDT),
            primitive.get(TOTAL_RECIPE_TICKS_KEY, PylonSerializers.INTEGER),
            primitive.get(RECIPE_TICKS_REMAINING_KEY, PylonSerializers.INTEGER),
            null
        )
    }

    override fun toPrimitive(complex: PylonRecipeProcessor.RecipeProcessorData, context: PersistentDataAdapterContext): PersistentDataContainer {
        val pdc = context.newPersistentDataContainer()
        val recipePDT = PylonSerializers.KEYED.keyedTypeFrom { complex.recipeType!!.getRecipeOrThrow(it) }
        pdc.setNullable(RECIPE_TYPE_KEY, RECIPE_TYPE_TYPE, complex.recipeType)
        pdc.setNullable(CURRENT_RECIPE_KEY, recipePDT, complex.currentRecipe)
        pdc.setNullable(TOTAL_RECIPE_TICKS_KEY, PylonSerializers.INTEGER, complex.totalRecipeTicks)
        pdc.setNullable(RECIPE_TICKS_REMAINING_KEY, PylonSerializers.INTEGER, complex.recipeTicksRemaining)
        return pdc
    }
}
