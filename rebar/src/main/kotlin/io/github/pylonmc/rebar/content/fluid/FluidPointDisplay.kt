package io.github.pylonmc.rebar.content.fluid

import io.github.pylonmc.rebar.fluid.VirtualFluidPoint
import org.bukkit.entity.ItemDisplay
import java.util.UUID

interface FluidPointDisplay {
    val entity: ItemDisplay
    val uuid: UUID
    val point: VirtualFluidPoint
    val connectedPipeDisplays: Set<UUID>

    fun connectPipeDisplay(uuid: UUID)
    fun disconnectPipeDisplay(uuid: UUID)
}