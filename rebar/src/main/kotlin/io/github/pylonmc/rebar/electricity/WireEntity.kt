package io.github.pylonmc.rebar.electricity

import io.github.pylonmc.rebar.config.RebarConfig
import io.github.pylonmc.rebar.datatypes.RebarSerializers
import io.github.pylonmc.rebar.electricity.nodes.ElectricNode
import io.github.pylonmc.rebar.entity.EntityStorage
import io.github.pylonmc.rebar.entity.RebarEntity
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder
import io.github.pylonmc.rebar.entity.display.transform.LineBuilder
import io.github.pylonmc.rebar.entity.interfaces.RemoveRebarEntityHandler
import io.github.pylonmc.rebar.i18n.RebarArgument
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder
import io.github.pylonmc.rebar.util.Either
import io.github.pylonmc.rebar.util.minus
import io.github.pylonmc.rebar.util.rebarKey
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityRemoveEvent
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.ceil

private typealias Port = Pair<ElectricNode, Location>

class WireEntity : RebarEntity<ItemDisplay>, RemoveRebarEntityHandler {

    var port: Port
        private set

    var otherEnd: Either<Player, Port>
        private set

    var length: Double = 0.0
        private set

    val wireCount: Int get() = ceil(length).toInt()

    constructor(port: Port, otherEnd: Either<Player, Port>) : super(
        KEY,
        ItemDisplayBuilder()
            .itemStack(ItemStackBuilder.of(Material.COPPER_BLOCK))
            .build(otherEnd.location)
    ) {
        this.port = port
        this.otherEnd = otherEnd
        EntityStorage.add(this)

        update()
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

        length = loc1.distance(loc2)
    }

    /**
     * Updates the state and visuals of the wire
     */
    fun update() {
        val loc1 = port.second
        val loc2 = otherEnd.location

        val midpoint = loc1.toVector().midpoint(loc2.toVector())
        entity.setTransformationMatrix(
            LineBuilder()
                .from(loc1.toVector() - midpoint)
                .to(loc2.toVector() - midpoint)
                .thickness(THICKNESS + 0.01f * ThreadLocalRandom.current().nextFloat())
                .build()
                .buildForItemDisplay()
        )
        entity.teleportAsync(midpoint.toLocation(loc1.world))

        length = loc1.distance(loc2)

        val player = (otherEnd as? Either.Left)?.value
        if (player != null && length > RebarConfig.MAX_WIRE_LENGTH) {
            player.sendMessage(
                Component.translatable(
                    "rebar.message.wiring.too_long",
                    RebarArgument.of("blocks", RebarConfig.MAX_WIRE_LENGTH)
                )
            )
            remove()
        }
    }

    /**
     * Makes the player hold the wire entity. Does *not* register the player as connecting in [WireConnectionService].
     * Does nothing if the wire is already being held by a player.
     */
    fun giveToPlayer(player: Player, disconnecting: ElectricNode) {
        val otherEnd = (this.otherEnd as? Either.Right)?.value ?: return
        otherEnd.first.disconnectFrom(port.first)

        if (disconnecting == port.first) {
            port = otherEnd
        }

        this.otherEnd = Either.Left(player)

        update()
    }

    fun connect(port1: Port, port2: Port) {
        (otherEnd as? Either.Right)?.value?.first?.disconnectFrom(port.first)
        port = port1
        otherEnd = Either.Right(port2)

        port1.first.connect(port2.first)

        update()
    }

    override fun onUnload() {
        val otherEnd = (this.otherEnd as? Either.Right)?.value

        if (otherEnd != null) {
            val pdc = entity.persistentDataContainer
            pdc.set(portKey, RebarSerializers.UUID, port.first.id)
            pdc.set(portLocKey, RebarSerializers.LOCATION, port.second)

            pdc.set(otherEndKey, RebarSerializers.UUID, otherEnd.first.id)
            pdc.set(otherEndLocKey, RebarSerializers.LOCATION, otherEnd.second)
        } else {
            // probably server shutdown
            remove()
        }
    }

    override fun onRemoved(event: EntityRemoveEvent, priority: EventPriority) {
        when (val otherEnd = otherEnd) {
            is Either.Left -> WireConnectionService.stopConnectingWire(otherEnd.value, delete = false)
            is Either.Right -> otherEnd.value.first.disconnectFrom(port.first)
        }
    }

    companion object {

        private val portKey = rebarKey("port")
        private val portLocKey = rebarKey("port_loc")
        private val otherEndKey = rebarKey("other_end")
        private val otherEndLocKey = rebarKey("other_end_loc")

        @JvmField
        val KEY = rebarKey("wire")

        const val THICKNESS = 0.05f
    }
}

private val Either<Player, Port>.location
    get() = when (this) {
        is Either.Left -> value.eyeLocation.subtract(0.0, 0.5, 0.0)
        is Either.Right -> value.second
    }