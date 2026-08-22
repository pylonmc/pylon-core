package io.github.pylonmc.rebar.electricity

import io.github.pylonmc.rebar.datatypes.RebarSerializers
import io.github.pylonmc.rebar.electricity.nodes.ElectricNode
import io.github.pylonmc.rebar.entity.EntityStorage
import io.github.pylonmc.rebar.entity.RebarEntity
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder
import io.github.pylonmc.rebar.entity.display.transform.LineBuilder
import io.github.pylonmc.rebar.entity.interfaces.RemoveRebarEntityHandler
import io.github.pylonmc.rebar.util.Either
import io.github.pylonmc.rebar.util.rebarKey
import org.bukkit.Location
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityRemoveEvent

private typealias Port = Pair<ElectricNode, Location>

class WireEntity : RebarEntity<ItemDisplay>, RemoveRebarEntityHandler {

    var port: Port
        private set

    var otherEnd: Either<Player, Port>
        private set

    constructor(port: Port, otherEnd: Either<Player, Port>) : super(
        KEY,
        ItemDisplayBuilder().build(otherEnd.location)
    ) {
        this.port = port
        this.otherEnd = otherEnd
        EntityStorage.add(this)

        updateTransformation()
    }

    @Suppress("unused")
    constructor(entity: ItemDisplay) : super(entity) {
        val pdc = entity.persistentDataContainer

        val node = ElectricityManager.getNodeById(pdc.get(portKey, RebarSerializers.UUID)!!)!!
        val loc1 = pdc.get(portLocKey, RebarSerializers.LOCATION)!!
        port = node to loc1

        val node2 = ElectricityManager.getNodeById(pdc.get(otherEndKey, RebarSerializers.UUID)!!)!!
        val loc2 = pdc.get(otherEndLocKey, RebarSerializers.LOCATION)!!
        otherEnd = Either.Right(node to loc2)
    }

    fun updateTransformation() {
        val loc1 = port.second
        val loc2 = otherEnd.location

        val midpoint = loc1.toVector().midpoint(loc2.toVector())
        entity.setTransformationMatrix(
            LineBuilder()
                .from(loc1.toVector().subtract(midpoint))
                .to(loc2.toVector().subtract(midpoint))
                .thickness(THICKNESS)
                .build()
                .buildForItemDisplay()
        )
        entity.teleportAsync(midpoint.toLocation(loc1.world))
    }

    override fun onUnload() {
        val otherEnd = (this.otherEnd as? Either.Right)?.value
            ?: throw IllegalStateException("Wire should not be unloaded while connected to player")

        val pdc = entity.persistentDataContainer
        pdc.set(portKey, RebarSerializers.UUID, port.first.id)
        pdc.set(portLocKey, RebarSerializers.LOCATION, port.second)

        pdc.set(otherEndKey, RebarSerializers.UUID, otherEnd.first.id)
        pdc.set(otherEndLocKey, RebarSerializers.LOCATION, otherEnd.second)
    }

    override fun onRemoved(event: EntityRemoveEvent, priority: EventPriority) {
        val otherEnd = (this.otherEnd as? Either.Right)?.value ?: return
        otherEnd.first.disconnectFrom(port.first)
    }

    companion object {

        private val portKey = rebarKey("port")
        private val portLocKey = rebarKey("port_loc")
        private val otherEndKey = rebarKey("other_end")
        private val otherEndLocKey = rebarKey("other_end_loc")

        @JvmField
        val KEY = rebarKey("wire")

        const val THICKNESS = 0.01f
    }
}

private val Either<Player, Port>.location
    get() = when (this) {
        is Either.Left -> value.eyeLocation.subtract(0.0, 0.5, 0.0)
        is Either.Right -> value.second
    }