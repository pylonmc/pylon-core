package io.github.pylonmc.pylon.core.content.fluid

import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.entity.EntityStorage
import io.github.pylonmc.pylon.core.entity.PylonEntity
import io.github.pylonmc.pylon.core.entity.display.ItemDisplayBuilder
import io.github.pylonmc.pylon.core.entity.display.transform.LineBuilder
import io.github.pylonmc.pylon.core.fluid.FluidManager
import io.github.pylonmc.pylon.core.fluid.connecting.ConnectingService
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
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
    val pipe: FluidPipe
    val amount: Int
    val from: UUID
    val to: UUID

    @Suppress("unused")
    constructor(entity: ItemDisplay) : super(entity) {
        val pdc = entity.persistentDataContainer

        // will fail to load if schema not found; no way around this
        pipe = PylonItem.fromStack(pdc.get(PIPE_KEY, PylonSerializers.ITEM_STACK)) as FluidPipe
        this.amount = pdc.get(AMOUNT_KEY, PylonSerializers.INTEGER)!!
        from = pdc.get(FROM_KEY, PylonSerializers.UUID)!!
        to = pdc.get(TO_KEY, PylonSerializers.UUID)!!

        // When fluid points are loaded back, their segment's fluid per second and predicate won't be preserved, so
        // we wait for them to load and then set their segments' fluid per second and predicate
        EntityStorage.whenEntityLoads(from, FluidPointInteraction::class.java) { interaction ->
            FluidManager.setFluidPerSecond(interaction.point.segment, pipe.fluidPerSecond)
            FluidManager.setFluidPredicate(interaction.point.segment, pipe::canPass)
        }

        // Technically only need to do this for one of the end points since they're part of the same segment, but
        // we do it twice just to be safe
        EntityStorage.whenEntityLoads(to, FluidPointInteraction::class.java) { interaction ->
            FluidManager.setFluidPerSecond(interaction.point.segment, pipe.fluidPerSecond)
            FluidManager.setFluidPredicate(interaction.point.segment, pipe::canPass)
        }
    }

    constructor(
        pipe: FluidPipe, amount: Int, from: FluidPointInteraction, to: FluidPointInteraction
    ) : super(KEY, makeDisplay(pipe, from, to)) {
        this.pipe = pipe
        this.amount = amount
        this.from = from.uuid
        this.to = to.uuid
    }

    override fun write(pdc: PersistentDataContainer) {
        pdc.set(PIPE_KEY, PylonSerializers.ITEM_STACK, pipe.stack)
        pdc.set(AMOUNT_KEY, PylonSerializers.INTEGER, amount)
        pdc.set(FROM_KEY, PylonSerializers.UUID, from)
        pdc.set(TO_KEY, PylonSerializers.UUID, to)
    }

    fun getFrom(): FluidPointInteraction
        = EntityStorage.getAs<FluidPointInteraction>(from)!!

    fun getTo(): FluidPointInteraction
        = EntityStorage.getAs<FluidPointInteraction>(to)!!

    fun delete(removeMarkersIfEmpty: Boolean, player: Player?, drops: MutableList<ItemStack>?) {
        val from = getFrom()
        val to = getTo()

        val itemToGive = pipe.stack.clone()
        itemToGive.amount = amount
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

        ConnectingService.disconnect(from, to, removeMarkersIfEmpty)
    }

    companion object {

        val KEY = pylonKey("fluid_pipe_display")

        private val AMOUNT_KEY = pylonKey("amount")
        private val PIPE_KEY = pylonKey("pipe")
        private val FROM_KEY = pylonKey("from")
        private val TO_KEY = pylonKey("to")

        private fun makeDisplay(pipe: FluidPipe, from: FluidPointInteraction, to: FluidPointInteraction): ItemDisplay {
            val height = from.entity.interactionHeight
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
                    .thickness(0.1)
                    .build()
                    .buildForItemDisplay()
                )
                .itemStack(ItemStackBuilder.of(pipe.material)
                    .addCustomModelDataString("fluid_pipe_display:${pipe.key.key}")
                )
                .build(centerLocation)
        }

        /**
         * Convenience function that constructs the display, but then also adds it to EntityStorage
         */
        @JvmStatic
        fun make(pipe: FluidPipe, amount: Int, from: FluidPointInteraction, to: FluidPointInteraction): FluidPipeDisplay {
            val display = FluidPipeDisplay(pipe, amount, from, to)
            EntityStorage.add(display)
            return display
        }
    }
}
