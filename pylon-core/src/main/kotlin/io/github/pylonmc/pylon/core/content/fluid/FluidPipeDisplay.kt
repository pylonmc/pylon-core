package io.github.pylonmc.pylon.core.content.fluid

import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.entity.EntityStorage
import io.github.pylonmc.pylon.core.entity.PylonEntity
import io.github.pylonmc.pylon.core.entity.display.ItemDisplayBuilder
import io.github.pylonmc.pylon.core.entity.display.transform.LineBuilder
import io.github.pylonmc.pylon.core.fluid.FluidManager
import io.github.pylonmc.pylon.core.fluid.placement.FluidPipePlacementService
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.GameMode
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import org.jetbrains.annotations.ApiStatus
import java.util.UUID

/**
 * A display that visually represents a pipe.
 */
@ApiStatus.Internal
class FluidPipeDisplay : PylonEntity<ItemDisplay> {
    val fromDisplay: UUID
    val toDisplay: UUID
    val pipe: FluidPipe
    val pipeAmount: Int

    constructor(
        pipe: FluidPipe, pipeAmount: Int, from: FluidPointDisplay, to: FluidPointDisplay
    ) : super(KEY, makeDisplay(pipe, from, to)) {
        this.pipe = pipe
        this.pipeAmount = pipeAmount
        this.fromDisplay = from.uuid
        this.toDisplay = to.uuid
        EntityStorage.add(this)
    }

    @Suppress("unused")
    constructor(entity: ItemDisplay) : super(entity) {
        val pdc = entity.persistentDataContainer

        this.fromDisplay = pdc.get(FROM_DISPLAY_KEY, PylonSerializers.UUID)!!
        this.toDisplay = pdc.get(TO_DISPLAY_KEY, PylonSerializers.UUID)!!

        // will fail to load if schema not found; no way around this
        val pipeSchema = pdc.get(PIPE_KEY, PIPE_TYPE)!!
        pipe = PylonItem.fromStack(pipeSchema.itemStack) as FluidPipe

        this.pipeAmount = pdc.get(PIPE_AMOUNT_KEY, PylonSerializers.INTEGER)!!

        // When fluid points are loaded back, their segment's fluid per second and predicate won't be preserved, so
        // we wait for them to load and then set their segments' fluid per second and predicate
        EntityStorage.whenEntityLoads<FluidPointDisplay>(fromDisplay) { display ->
            FluidManager.setFluidPerSecond(display.point.segment, pipe.fluidPerSecond)
            FluidManager.setFluidPredicate(display.point.segment, pipe::canPass)
        }

        // Technically only need to do this for one of the end points since they're part of the same segment, but
        // we do it twice just to be safe
        EntityStorage.whenEntityLoads<FluidPointDisplay>(toDisplay) { display ->
            FluidManager.setFluidPerSecond(display.point.segment, pipe.fluidPerSecond)
            FluidManager.setFluidPredicate(display.point.segment, pipe::canPass)
        }
    }

    override fun write(pdc: PersistentDataContainer) {
        pdc.set(FROM_DISPLAY_KEY, PylonSerializers.UUID, fromDisplay)
        pdc.set(TO_DISPLAY_KEY, PylonSerializers.UUID, toDisplay)
        pdc.set(PIPE_KEY, PIPE_TYPE, pipe.schema)
        pdc.set(PIPE_AMOUNT_KEY, PylonSerializers.INTEGER, pipeAmount)
    }

    fun getFrom(): FluidPointDisplay
        = EntityStorage.getAs<FluidPointDisplay>(fromDisplay)!!

    fun getTo(): FluidPointDisplay
        = EntityStorage.getAs<FluidPointDisplay>(toDisplay)!!

    fun delete(player: Player?, drops: MutableList<ItemStack>?) {
        if (!entity.isValid) {
            // already deleted
            return
        }

        val from = getFrom()
        val to = getTo()

        val itemToGive = pipe.stack.clone()
        itemToGive.amount = pipeAmount
        if (player != null) {
            if (player.gameMode != GameMode.CREATIVE) {
                player.give(itemToGive)
            }
        } else if (drops != null) {
            drops.add(itemToGive)
        } else {
            val location = to.point.position.plus(from.point.position).location.multiply(0.5)
            location.getWorld().dropItemNaturally(location, itemToGive)
        }

        FluidPipePlacementService.disconnect(from, to, true)
    }

    companion object {

        val KEY = pylonKey("fluid_pipe_display")
        const val SIZE = 0.1

        private val PIPE_AMOUNT_KEY = pylonKey("pipe_amount")
        private val PIPE_KEY = pylonKey("pipe")
        private val PIPE_TYPE = PylonSerializers.KEYED.keyedTypeFrom { PylonRegistry.ITEMS.getOrThrow(it) }
        private val FROM_DISPLAY_KEY = pylonKey("from_display")
        private val TO_DISPLAY_KEY = pylonKey("to_display")

        private fun makeDisplay(pipe: FluidPipe, from: FluidPointDisplay, to: FluidPointDisplay): ItemDisplay {
            val height = from.entity.height
            val fromLocation = from.entity.location.add(0.0, height / 2.0, 0.0)
            val toLocation = to.entity.location.add(0.0, height / 2.0, 0.0)
            // We use a center location rather than just spawning at fromLocation or toLocation to prevent the entity
            // from being spawned just inside a block - this causes it to render as black due to being inside the block
            val centerLocation = fromLocation.clone().add(toLocation).multiply(0.5)
            val fromOffset = centerLocation.clone().subtract(fromLocation).toVector().toVector3f()
            val toOffset = centerLocation.clone().subtract(toLocation).toVector().toVector3f()

            return ItemDisplayBuilder()
                .transformation(LineBuilder()
                    .from(fromOffset)
                    .to(toOffset)
                    .thickness(SIZE)
                    .build()
                    .buildForItemDisplay()
                )
                .itemStack(ItemStackBuilder.of(pipe.material)
                    .addCustomModelDataString("fluid_pipe_display:${pipe.key.key}")
                )
                .build(centerLocation)
        }
    }
}
