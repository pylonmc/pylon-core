package io.github.pylonmc.pylon.core.content.fluid

import io.github.pylonmc.pylon.core.block.PylonBlock
import io.github.pylonmc.pylon.core.block.base.PylonBreakHandler
import io.github.pylonmc.pylon.core.block.base.PylonEntityHolderBlock
import io.github.pylonmc.pylon.core.block.context.BlockBreakContext
import io.github.pylonmc.pylon.core.block.context.BlockBreakContext.PlayerBreak
import io.github.pylonmc.pylon.core.block.context.BlockCreateContext
import io.github.pylonmc.pylon.core.entity.EntityStorage
import io.github.pylonmc.pylon.core.i18n.PylonArgument
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.util.pylonKey
import io.github.pylonmc.pylon.core.waila.WailaDisplay
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer

/**
 * A 'fluid pipe connector' is one of the small gray displays that appears
 * on pipe corners/junctions.
 */
class FluidIntersectionMarker : PylonBlock, PylonEntityHolderBlock, PylonBreakHandler {

    @Suppress("unused")
    constructor(block: Block, context: BlockCreateContext) : super(block) {
        addEntity("intersection", FluidIntersectionDisplay(block))
    }

    @Suppress("unused")
    constructor(block: Block, pdc: PersistentDataContainer) : super(block)

    val fluidIntersectionDisplay
        get() = getHeldPylonEntityOrThrow(FluidIntersectionDisplay::class.java, "intersection")

    override fun onBreak(drops: MutableList<ItemStack>, context: BlockBreakContext) {
        var player: Player? = if (context is PlayerBreak) context.event.player else null

        // Clone to prevent ConcurrentModificationException if pipeDisplay.delete modified connectedPipeDisplays
        for (pipeDisplayId in fluidIntersectionDisplay.connectedPipeDisplays.toSet()) {
            val pipeDisplay = EntityStorage.getAs<FluidPipeDisplay>(pipeDisplayId)
            // can be null if called from two different location (eg two different connection points removing the display)
            pipeDisplay?.delete(player, drops)
        }
    }

    override fun getWaila(player: Player): WailaDisplay?
        = WailaDisplay(defaultWailaTranslationKey.arguments(PylonArgument.of("pipe", this.pipe.stack.effectiveName())))

    val pipe: PylonItem
        get() {
            check(fluidIntersectionDisplay.connectedPipeDisplays.isNotEmpty())
            val uuid = fluidIntersectionDisplay.connectedPipeDisplays.iterator().next()
            return EntityStorage.getAs<FluidPipeDisplay?>(uuid)!!.pipe
        }

    override fun getDropItem(context: BlockBreakContext) = null

    override fun getPickItem() = pipe.stack

    companion object {
        @JvmField
        val KEY = pylonKey("fluid_pipe_intersection_marker")
    }
}
