package io.github.pylonmc.pylon.core.content.fluid

import io.github.pylonmc.pylon.core.entity.EntityStorage
import io.github.pylonmc.pylon.core.entity.PylonEntity
import io.github.pylonmc.pylon.core.entity.display.ItemDisplayBuilder
import io.github.pylonmc.pylon.core.entity.display.transform.TransformBuilder
import io.github.pylonmc.pylon.core.fluid.VirtualFluidPoint
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.entity.ItemDisplay
import org.bukkit.util.Vector

class FluidPointDisplay(entity: ItemDisplay) : PylonEntity<ItemDisplay>(KEY, entity) {

    companion object {

        val KEY = pylonKey("fluid_connection_display")

        fun make(point: VirtualFluidPoint, translation: Vector): FluidPointDisplay {
            val display = ItemDisplayBuilder()
                .material(point.type.material)
                .brightness(7)
                .transformation(TransformBuilder()
                    .translate(translation.toVector3d())
                    .scale(FluidPointInteraction.POINT_SIZE)
                )
                .build(point.position.location.toCenterLocation())
            val entity = FluidPointDisplay(display)
            EntityStorage.add(entity)
            return entity
        }
    }
}
