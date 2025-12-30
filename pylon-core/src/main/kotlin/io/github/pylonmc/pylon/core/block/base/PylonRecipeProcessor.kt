package io.github.pylonmc.pylon.core.block.base

import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.event.PylonBlockDeserializeEvent
import io.github.pylonmc.pylon.core.event.PylonBlockLoadEvent
import io.github.pylonmc.pylon.core.event.PylonBlockSerializeEvent
import io.github.pylonmc.pylon.core.event.PylonBlockUnloadEvent
import io.github.pylonmc.pylon.core.recipe.PylonRecipe
import io.github.pylonmc.pylon.core.recipe.RecipeType
import io.github.pylonmc.pylon.core.util.gui.ProgressItem
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.jetbrains.annotations.ApiStatus
import java.util.IdentityHashMap

/**
 * An interface that stores and progresses a recipe.
 *
 * You can set a progress item with `setRecipeProgressItem`. This item
 * will be automatically synchronized to the recipe progress, and will
 * be persisted.
 *
 * @see PylonProcessor
 */
interface PylonRecipeProcessor<T: PylonRecipe> {

    @ApiStatus.Internal
    data class RecipeProcessorData(
        var recipeType: RecipeType<*>?,
        var currentRecipe: PylonRecipe?,
        var recipeTimeTicks: Int?,
        var recipeTicksRemaining: Int?,
        var progressItem: ProgressItem?,
    )

    private val recipeProcessorData: RecipeProcessorData
        @ApiStatus.NonExtendable
        get() = recipeProcessorBlocks.getOrPut(this) { RecipeProcessorData(null, null, null, null, null)}

    val currentRecipe: T?
        @ApiStatus.NonExtendable
        // cast should always be safe due to type restriction when starting recipe
        get() = recipeProcessorData.currentRecipe as T?

    val recipeTicksRemaining: Int?
        @ApiStatus.NonExtendable
        get() = recipeProcessorData.recipeTicksRemaining

    val isProcessingRecipe: Boolean
        @ApiStatus.NonExtendable
        get() = currentRecipe != null

    var recipeProgressItem: ProgressItem
        get() = recipeProcessorData.progressItem ?: error("No recipe progress item was set")
        set(progressItem) {
            recipeProcessorData.progressItem = progressItem
        }

    /**
     * Set the progress item that should be updated as the recipe progresses. Optional.
     *
     * Set once in your place constructor.
     */
    @ApiStatus.NonExtendable
    fun setRecipeType(type: RecipeType<T>) {
        recipeProcessorData.recipeType = type
    }

    /**
     * Starts a new recipe with duration [ticks], with [ticks] being the number of server
     * ticks the recipe will take.
     */
    fun startRecipe(recipe: T, ticks: Int) {
        recipeProcessorData.currentRecipe = recipe
        recipeProcessorData.recipeTimeTicks = ticks
        recipeProcessorData.recipeTicksRemaining = ticks
        recipeProcessorData.progressItem?.setTotalTimeTicks(ticks)
        recipeProcessorData.progressItem?.setRemainingTimeTicks(ticks)
    }

    fun stopRecipe() {
        val data = recipeProcessorData
        data.currentRecipe = null
        data.recipeTimeTicks = null
        data.recipeTicksRemaining = null
        data.progressItem?.totalTime = null
    }

    fun finishRecipe() {
        check(isProcessingRecipe) {
            "Cannot finish recipe because there is no recipe being processed"
        }
        @Suppress("UNCHECKED_CAST") // cast should always be safe due to type restriction when starting recipe
        val currentRecipe = recipeProcessorData.currentRecipe as T
        stopRecipe()
        onRecipeFinished(currentRecipe)
    }

    fun onRecipeFinished(recipe: T)

    fun progressRecipe(ticks: Int) {
        val data = recipeProcessorData
        if (data.currentRecipe != null && data.recipeTicksRemaining != null) {
            data.recipeTicksRemaining = data.recipeTicksRemaining!! - ticks
            data.progressItem?.setRemainingTimeTicks(data.recipeTicksRemaining!!)
            if (data.recipeTicksRemaining!! <= 0) {
                finishRecipe()
            }
        }
    }

    @ApiStatus.Internal
    companion object : Listener {

        private val recipeProcessorKey = pylonKey("recipe_processor_data")

        private val recipeProcessorBlocks = IdentityHashMap<PylonRecipeProcessor<*>, RecipeProcessorData>()

        @EventHandler
        private fun onDeserialize(event: PylonBlockDeserializeEvent) {
            val block = event.pylonBlock
            if (block is PylonRecipeProcessor<*>) {
                val data = event.pdc.get(recipeProcessorKey, PylonSerializers.RECIPE_PROCESSOR_DATA)
                    ?: error("Recipe processor data not found for ${block.key}")
                recipeProcessorBlocks[block] = data
            }
        }

        @EventHandler
        private fun onLoad(event: PylonBlockLoadEvent) {
            // This separate listener is needed because when [PylonBlockDeserializeEvent] fires, then the
            // block may not have been fully initialised yet (e.g. postInitialise may not have been called)
            // which means progressItem may not have been set yet
            val block = event.pylonBlock
            if (block is PylonRecipeProcessor<*>) {
                val data = recipeProcessorBlocks[block]!!
                data.progressItem?.setTotalTimeTicks(data.recipeTimeTicks)
                data.recipeTicksRemaining?.let { data.progressItem?.setRemainingTimeTicks(it) }
            }
        }

        @EventHandler
        private fun onSerialize(event: PylonBlockSerializeEvent) {
            val block = event.pylonBlock
            if (block is PylonRecipeProcessor<*>) {
                val data = recipeProcessorBlocks[block] ?: error {
                    "No recipe processor data found for ${block.key}"
                }
                event.pdc.set(recipeProcessorKey, PylonSerializers.RECIPE_PROCESSOR_DATA, data)
                check(data.recipeType != null) { "No recipe type set for ${event.pylonBlock.key}; did you forget to call setRecipeType in your place constructor?" }
            }
        }

        @EventHandler
        private fun onUnload(event: PylonBlockUnloadEvent) {
            val block = event.pylonBlock
            if (block is PylonRecipeProcessor<*>) {
                recipeProcessorBlocks.remove(block)
            }
        }
    }
}