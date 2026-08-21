package io.github.pylonmc.rebar.electricity.nodes

import io.github.pylonmc.rebar.datatypes.RebarSerializers
import io.github.pylonmc.rebar.entity.EntityStorage
import io.github.pylonmc.rebar.entity.RebarEntity
import io.github.pylonmc.rebar.entity.display.InteractionBuilder
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder
import io.github.pylonmc.rebar.entity.interfaces.RemoveRebarEntityHandler
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder
import io.github.pylonmc.rebar.util.rebarKey
import org.bukkit.Bukkit
import org.bukkit.block.Block
import org.bukkit.entity.Interaction
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityRemoveEvent
import kotlin.math.PI

class ElectricPortEntity : RebarEntity<Interaction>, RemoveRebarEntityHandler {

    constructor(block: Block, port: ElectricPort) : super(
        KEY,
        InteractionBuilder()
            .size(SCALE)
            .build(block.location.toCenterLocation().add(port.face.direction.multiply(port.radius)).add(port.offset))
    ) {
        val display = ItemDisplayBuilder()
            .itemStack(ItemStackBuilder.of(port.material).addCustomModelDataString("electric_port"))
            .transformation(
                TransformBuilder()
                    .rotate(port.face.direction.toVector3d(), PI / 4)
                    .translate(port.face.direction.multiply(port.radius * -0.01).add(port.offset).toVector3d())
                    .scale(SCALE)
            )
            .build(block.location.toCenterLocation().add(port.face.direction.multiply(port.radius * 1.01)))

        entity.persistentDataContainer.set(displayKey, RebarSerializers.UUID, display.uniqueId)

        EntityStorage.add(this)
    }

    constructor(entity: Interaction) : super(entity)

    override fun onRemoved(event: EntityRemoveEvent, priority: EventPriority) {
        Bukkit.getEntity(entity.persistentDataContainer.get(displayKey, RebarSerializers.UUID)!!)!!.remove()
    }

    companion object {

        private val displayKey = rebarKey("display")

        @JvmField
        val KEY = rebarKey("electric_port")

        const val SCALE = 0.19f
    }
}