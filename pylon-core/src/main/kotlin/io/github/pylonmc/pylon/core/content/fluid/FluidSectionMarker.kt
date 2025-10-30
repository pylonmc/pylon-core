package io.github.pylonmc.pylon.core.content.fluid

import io.github.pylonmc.pylon.core.block.PylonBlock
import io.github.pylonmc.pylon.core.block.base.PylonBreakHandler
import io.github.pylonmc.pylon.core.block.base.PylonEntityHolderBlock
import io.github.pylonmc.pylon.core.block.context.BlockBreakContext
import io.github.pylonmc.pylon.core.block.context.BlockBreakContext.PlayerBreak
import io.github.pylonmc.pylon.core.block.context.BlockCreateContext
import io.github.pylonmc.pylon.core.i18n.PylonArgument
import io.github.pylonmc.pylon.core.util.pylonKey
import io.github.pylonmc.pylon.core.waila.WailaDisplay
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer

/**
 * An invisible block (structure block) that exists purely to represent a pipe and prevent
 * blocks from being placed on top of them.
 */
class FluidSectionMarker : PylonBlock, PylonBreakHandler, PylonEntityHolderBlock {
    override var disableBlockTextureEntity = true

    @Suppress("unused")
    constructor(block: Block, context: BlockCreateContext) : super(block)

    @Suppress("unused")
    constructor(block: Block, pdc: PersistentDataContainer) : super(block)

    val pipeDisplay
        get() = getHeldPylonEntity(FluidPipeDisplay::class.java, "pipe")

    val pipe
        get() = pipeDisplay?.pipe

    override fun onBreak(drops: MutableList<ItemStack>, context: BlockBreakContext) {
        var player: Player? = null
        if (context is PlayerBreak) {
            player = context.event.player
        }

        pipeDisplay?.delete(player, drops)
    }

    override fun getWaila(player: Player): WailaDisplay?
        = WailaDisplay(defaultWailaTranslationKey.arguments(PylonArgument.of("pipe", pipe!!.stack.effectiveName())))

    override fun getDropItem(context: BlockBreakContext) = null

    override fun getPickItem() = pipe!!.stack

    companion object {
        val KEY = pylonKey("fluid_pipe_section_marker")
    }
}
