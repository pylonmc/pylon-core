package io.github.pylonmc.pylon.core.content.fluid

import io.github.pylonmc.pylon.core.block.PylonBlock
import io.github.pylonmc.pylon.core.block.base.PylonEntityHolderBlock
import io.github.pylonmc.pylon.core.block.context.BlockBreakContext
import io.github.pylonmc.pylon.core.block.context.BlockBreakContext.PlayerBreak
import io.github.pylonmc.pylon.core.block.context.BlockCreateContext
import io.github.pylonmc.pylon.core.block.waila.WailaConfig
import io.github.pylonmc.pylon.core.entity.EntityStorage
import io.github.pylonmc.pylon.core.fluid.FluidPointType
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer

class FluidPipeConnector : PylonBlock, PylonEntityHolderBlock {

    @Suppress("unused")
    constructor(block: Block, context: BlockCreateContext) : super(block)

    @Suppress("unused")
    constructor(block: Block, pdc: PersistentDataContainer) : super(block)

    override fun createEntities(context: BlockCreateContext)
        = mutableMapOf(
            Pair("connector", FluidPointInteraction.make(context, FluidPointType.CONNECTOR))
        )

    val fluidPointInteraction
        get() = getHeldEntityOrThrow(FluidPointInteraction::class.java, "connector")

    override fun onBreak(drops: MutableList<ItemStack>, context: BlockBreakContext) {
        var player: Player? = null
        if (context is PlayerBreak) {
            player = context.event.player
        }

        // Clone to prevent ConcurrentModificationException if pipeDisplay.delete modified connectedPipeDisplays
        for (pipeDisplayId in HashSet(fluidPointInteraction.connectedPipeDisplays)) {
            val pipeDisplay = EntityStorage.getAs<FluidPipeDisplay>(pipeDisplayId)
            // can be null if called from two different location (eg two different connection points removing the display)
            pipeDisplay?.delete(true, player)
        }

        super<PylonEntityHolderBlock>.onBreak(drops, context)
    }

    override fun getWaila(player: Player): WailaConfig
        = WailaConfig(name, mapOf(Pair("pipe", this.pipe.stack.effectiveName())))

    val pipe: PylonItem
        get() {
            check(!fluidPointInteraction.connectedPipeDisplays.isEmpty())
            val uuid = fluidPointInteraction.connectedPipeDisplays.iterator().next()
            return  EntityStorage.getAs<FluidPipeDisplay?>(uuid)!!.pipe
        }

    companion object {
        val KEY = pylonKey("fluid_pipe_connector")
    }
}
