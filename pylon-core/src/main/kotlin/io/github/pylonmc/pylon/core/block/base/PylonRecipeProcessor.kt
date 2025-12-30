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
 * This does not actually handle the recipe inputs and outputs. Instead, it simply
 * tracks a recipe that is being processed and how much time is left on it, ticking
 * automatically.
 *
 * This interface overrides [PylonTickingBlock.tick], meaning the rate at which the
 * recipe ticks is determined by [PylonTickingBlock.setTickInterval].
 */
interface PylonRecipeProcessor<T: PylonRecipe> : PylonTickingBlock {

    data class RecipeProcessorData(
        var recipeType: RecipeType<*>?,
        var currentRecipe: PylonRecipe?,
        var totalRecipeTicks: Int?,
        var recipeTicksRemaining: Int?,
        var progressItem: ProgressItem?,
    )

    private val recipeProcessorData: RecipeProcessorData
        get() = recipeProcessorBlocks.getOrPut(this) { RecipeProcessorData(null, null, null, null, null)}

    val currentRecipe: T?
        // cast should always be safe due to type restriction when starting recipe
        get() = recipeProcessorData.currentRecipe as T?

    val recipeTicksRemaining: Int?
        get() = recipeProcessorData.recipeTicksRemaining

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
     * Set the progress item that should be updated as the recipe progresses. Optional.
     *
     * Does not persist; you must call this whenever the block is initialised (e.g.
     * in [io.github.pylonmc.pylon.core.block.PylonBlock.postInitialise])
     */
    @ApiStatus.NonExtendable
    fun setProgressItem(item: ProgressItem) {
        recipeProcessorData.progressItem = item
    }

    /**
     * Starts a new recipe with duration [ticks], with [ticks] being the number of server
     * ticks the recipe will take.
     */
    fun startRecipe(recipe: T, ticks: Int) {
        recipeProcessorData.currentRecipe = recipe
        recipeProcessorData.totalRecipeTicks = ticks
        recipeProcessorData.recipeTicksRemaining = ticks
        recipeProcessorData.progressItem?.setTotalTimeTicks(ticks)
        recipeProcessorData.progressItem?.setRemainingTimeTicks(ticks)
    }

    fun onRecipeFinished(recipe: T)

    override fun tick(deltaSeconds: Double) {
        val data = recipeProcessorData

        if (data.currentRecipe != null && data.recipeTicksRemaining != null) {
            data.progressItem?.setRemainingTimeTicks(data.recipeTicksRemaining!!)

            // tick recipe
            if (data.recipeTicksRemaining!! > 0) {
                data.recipeTicksRemaining = data.recipeTicksRemaining!! - tickInterval
                return
            }

            // finish recipe
            onRecipeFinished(data.currentRecipe as T)
            data.currentRecipe = null
            data.totalRecipeTicks = null
            data.recipeTicksRemaining = null
            data.progressItem?.totalTime = null
            // cast should always be safe due to type restriction when starting recipe
            return
        }
    }

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
                data.progressItem?.setTotalTimeTicks(data.totalRecipeTicks)
                data.recipeTicksRemaining?.let { data.progressItem?.setRemainingTimeTicks(it) }
            }
        }

        @EventHandler
        private fun onSerialize(event: PylonBlockSerializeEvent) {
            val block = event.pylonBlock
            if (block is PylonRecipeProcessor<*>) {
                event.pdc.set(recipeProcessorKey, PylonSerializers.RECIPE_PROCESSOR_DATA, recipeProcessorBlocks[block]!!)
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