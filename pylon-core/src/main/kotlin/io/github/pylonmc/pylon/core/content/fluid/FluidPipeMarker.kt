package io.github.pylonmc.pylon.core.content.fluid

import io.github.pylonmc.pylon.core.block.PylonBlock
import io.github.pylonmc.pylon.core.block.base.PylonBreakHandler
import io.github.pylonmc.pylon.core.block.context.BlockBreakContext
import io.github.pylonmc.pylon.core.block.context.BlockBreakContext.PlayerBreak
import io.github.pylonmc.pylon.core.block.context.BlockBreakContext.PluginBreak
import io.github.pylonmc.pylon.core.block.context.BlockCreateContext
import io.github.pylonmc.pylon.core.block.context.BlockItemContext
import io.github.pylonmc.pylon.core.block.waila.WailaConfig
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.entity.EntityStorage
import io.github.pylonmc.pylon.core.i18n.PylonArgument
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import java.util.UUID

class FluidPipeMarker : PylonBlock, PylonBreakHandler {
    // Should always be set immediately after the marker has been placed
    var pipeDisplay: UUID? = null
    var from: UUID? = null
    var to: UUID? = null

    override var disableBlockTextureEntity = true

    @Suppress("unused")
    constructor(block: Block, context: BlockCreateContext) : super(block)

    @Suppress("unused")
    constructor(block: Block, pdc: PersistentDataContainer) : super(block) {
        pipeDisplay = pdc.get(PIPE_DISPLAY_KEY, PylonSerializers.UUID)
        from = pdc.get(FROM_KEY, PylonSerializers.UUID)
        to = pdc.get(TO_KEY, PylonSerializers.UUID)
    }

    override fun write(pdc: PersistentDataContainer) {
        pdc.set(PIPE_DISPLAY_KEY, PylonSerializers.UUID, pipeDisplay!!)
        pdc.set(FROM_KEY, PylonSerializers.UUID, from!!)
        pdc.set(TO_KEY, PylonSerializers.UUID, to!!)
    }

    fun getPipeDisplay(): FluidPipeDisplay?
        = EntityStorage.getAs<FluidPipeDisplay>(pipeDisplay!!)

    fun getFrom(): FluidPointInteraction
        = EntityStorage.getAs<FluidPointInteraction>(from!!)!!

    fun getTo(): FluidPointInteraction
        = EntityStorage.getAs<FluidPointInteraction>(to!!)!!

    override fun onBreak(drops: MutableList<ItemStack>, context: BlockBreakContext) {
        var player: Player? = null
        if (context is PlayerBreak) {
            player = context.event.player
        }

        // if this is triggered by a fluid connector being broken, the pipe display will already have been deleted
        // not the ideal solution, but can't think of anything better
        if (context !is PluginBreak) {
            getPipeDisplay()?.delete(true, player)
        }
    }

    override fun getWaila(player: Player): WailaConfig?
        = WailaConfig(defaultTranslationKey.arguments(PylonArgument.of("pipe", getPipeDisplay()!!.pipe.stack.effectiveName())))

    override fun getItem(context: BlockItemContext): ItemStack? {
        // Breaking is handled by other fluid pipe logic
        if (context is BlockItemContext.PickBlock) {
            return getPipeDisplay()?.pipe?.stack
        }
        return null
    }

    companion object {
        val KEY = pylonKey("fluid_pipe_marker")

        private val PIPE_DISPLAY_KEY = pylonKey("pipe_display")
        private val FROM_KEY = pylonKey("from")
        private val TO_KEY = pylonKey("to")
    }
}
