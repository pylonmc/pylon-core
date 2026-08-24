package io.github.pylonmc.rebar.electricity.nodes

import io.github.pylonmc.rebar.datatypes.RebarSerializers
import io.github.pylonmc.rebar.electricity.ElectricityManager
import io.github.pylonmc.rebar.electricity.WireConnectionService
import io.github.pylonmc.rebar.electricity.WireEntity
import io.github.pylonmc.rebar.entity.EntityStorage
import io.github.pylonmc.rebar.entity.RebarEntity
import io.github.pylonmc.rebar.entity.display.InteractionBuilder
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder
import io.github.pylonmc.rebar.entity.interfaces.InteractRebarEntityHandler
import io.github.pylonmc.rebar.entity.interfaces.RemoveRebarEntityHandler
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder
import io.github.pylonmc.rebar.util.Either
import io.github.pylonmc.rebar.util.rebarKey
import org.bukkit.Bukkit
import org.bukkit.block.Block
import org.bukkit.entity.Interaction
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityRemoveEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import kotlin.math.PI

// aka ActuallyTwoEntitiesInATrenchcoat
class ElectricPortEntity : RebarEntity<Interaction>, RemoveRebarEntityHandler, InteractRebarEntityHandler {

    val node: ElectricNode

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
                    // why all the math? well the port itself needs to exist slightly outside the radius
                    // to get proper lighting, so we spawn it there and offset it back.
                    // everything after the - is just offsetting it back further so it won't stick out
                    .translate(port.face.direction.multiply(port.radius * -0.01 - SCALE / 2 * 0.99).add(port.offset).toVector3d())
                    .scale(SCALE)
            )
            .build(block.location.toCenterLocation().add(port.face.direction.multiply(port.radius * 1.01)))

        entity.persistentDataContainer.set(displayKey, RebarSerializers.UUID, display.uniqueId)

        node = port.node
        entity.persistentDataContainer.set(nodeKey, RebarSerializers.UUID, node.id)

        EntityStorage.add(this)
    }

    @Suppress("unused")
    constructor(entity: Interaction) : super(entity) {
        node = ElectricityManager.getNodeById(entity.persistentDataContainer.get(nodeKey, RebarSerializers.UUID)!!)!!
    }

    override fun onRemoved(event: EntityRemoveEvent, priority: EventPriority) {
        @Suppress("UNCHECKED_CAST")
        for (wire in EntityStorage.getByKey(WireEntity.KEY) as Collection<WireEntity>) {
            if (wire.port.first == node || (wire.otherEnd as? Either.Right)?.value?.first == node) {
                wire.remove()
            }
        }
        Bukkit.getEntity(entity.persistentDataContainer.get(displayKey, RebarSerializers.UUID)!!)!!.remove()
    }

    override fun onInteractedWith(event: PlayerInteractEntityEvent, priority: EventPriority) {
        val player = event.player
        val wire = WireConnectionService.getWirePlayerIsConnecting(player)

        if (wire == null) {
            @Suppress("UNCHECKED_CAST")
            val wires = EntityStorage.getByKey(WireEntity.KEY) as Collection<WireEntity>
            val existingWire = wires
                .filter { it.port.first == node || (it.otherEnd as? Either.Right)?.value?.first == node }
                .maxByOrNull { it.length }
            val wire = existingWire ?: WireEntity(node to entity.location, Either.Left(player))
            wire.giveToPlayer(player, node)
            WireConnectionService.startConnectingWire(player, wire)
        } else if (wire.port.first == node) {
            WireConnectionService.stopConnectingWire(player)
        } else {
            val otherPort = wire.port
            wire.connect(otherPort, node to entity.location)
            WireConnectionService.stopConnectingWire(player, delete = false)
        }
    }

    companion object {

        private val displayKey = rebarKey("display")
        private val nodeKey = rebarKey("node")

        @JvmField
        val KEY = rebarKey("electric_port")

        const val SCALE = 0.19
    }
}