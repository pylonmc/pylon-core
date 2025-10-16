package io.github.pylonmc.pylon.core.content.fluid

import io.github.pylonmc.pylon.core.fluid.VirtualFluidPoint
import org.bukkit.entity.ItemDisplay
import org.jetbrains.annotations.ApiStatus
import java.util.UUID

@ApiStatus.Internal
interface FluidPointDisplay {
    val entity: ItemDisplay
    val uuid: UUID
    val point: VirtualFluidPoint
    val connectedPipeDisplays: Set<UUID>

    fun connectPipeDisplay(uuid: UUID)
    fun disconnectPipeDisplay(uuid: UUID)
}