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
import io.github.pylonmc.rebar.item.RebarItem
import io.github.pylonmc.rebar.item.RebarItemSchema
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder
import io.github.pylonmc.rebar.item.interfaces.WireRebarItem
import io.github.pylonmc.rebar.registry.RebarRegistry
import io.github.pylonmc.rebar.util.Either
import io.github.pylonmc.rebar.util.minus
import io.github.pylonmc.rebar.util.rebarKey
import net.kyori.adventure.text.Component
import org.bukkit.FluidCollisionMode
import org.bukkit.Location
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityRemoveEvent
import org.joml.Matrix4f
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

    val wireCount: Int get() = wiresRequired(length)

    var wire: RebarItemSchema
        private set

    constructor(port: Port, otherEnd: Either<Player, Port>, wireItem: WireRebarItem) : super(
        KEY,
        ItemDisplayBuilder()
            .transformation(getTransform(port.second, otherEnd.location))
            .build(port.second)
    ) {
        this.port = port
        this.otherEnd = otherEnd
        this.wire = (wireItem as RebarItem).schema
        this.length = port.second.distance(otherEnd.location)
        EntityStorage.add(this)
        setWireItem(wireItem)
    }

    @Suppress("unused")
    constructor(entity: ItemDisplay) : super(entity) {
        val pdc = entity.persistentDataContainer

        val node = ElectricityManager.getNodeById(pdc.get(portKey, RebarSerializers.UUID)!!)!!
        val loc1 = pdc.get(portLocKey, RebarSerializers.LOCATION)!!
        port = node to loc1

        val node2 = ElectricityManager.getNodeById(pdc.get(otherEndKey, RebarSerializers.UUID)!!)!!
        val loc2 = pdc.get(otherEndLocKey, RebarSerializers.LOCATION)!!
        otherEnd = Either.Right(node2 to loc2)

        wire = pdc.get(itemKey, itemType)!!

        length = loc1.distance(loc2)
    }

    fun setWireItem(wireItem: WireRebarItem) {
        this.wire = (wireItem as RebarItem).schema
        entity.setItemStack(ItemStackBuilder.of(wireItem.displayMaterial).addCustomModelDataString("wire").build())
    }

    val isHeldByPlayer: Boolean get() = otherEnd is Either.Left

    val isObstructed: Boolean get() = isObstructed(port.second, otherEnd.location, length)

    /**
     * Updates the state and visuals of the wire
     */
    fun update() {
        val loc1 = port.second
        val loc2 = otherEnd.location

        entity.setTransformationMatrix(getTransform(loc1, loc2))
        entity.interpolationDelay = 0
        entity.interpolationDuration = 1
        if (entity.location != loc1) {
            entity.teleportAsync(loc1) // in case port was flipped
        }

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

    /**
     * Connects the wire between the two ports. Returns the failure reason if failed
     */
    fun connect(port1: Port, port2: Port): ConnectionFailureReason? {
        val connection = canConnect(port1.second, port2.second)
        if (connection is Either.Right) {
            return connection.value
        }

        (otherEnd as? Either.Right)?.value?.first?.disconnectFrom(port.first)
        port = port1
        otherEnd = Either.Right(port2)

        port1.first.connect(port2.first)

        update()

        return null
    }

    override fun onUnload() {
        val otherEnd = (this.otherEnd as? Either.Right)?.value

        if (otherEnd != null) {
            val pdc = entity.persistentDataContainer
            pdc.set(portKey, RebarSerializers.UUID, port.first.id)
            pdc.set(portLocKey, RebarSerializers.LOCATION, port.second)

            pdc.set(otherEndKey, RebarSerializers.UUID, otherEnd.first.id)
            pdc.set(otherEndLocKey, RebarSerializers.LOCATION, otherEnd.second)

            pdc.set(itemKey, itemType, wire)
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
        private val itemKey = rebarKey("item")
        private val itemType = RebarSerializers.KEYED.fromRegistry(RebarRegistry.ITEMS)

        @JvmField
        val KEY = rebarKey("wire")

        const val THICKNESS = 0.05f

        private fun getTransform(start: Location, end: Location): Matrix4f {
            return LineBuilder()
                .from(start.toVector() - start.toVector())
                .to(end.toVector() - start.toVector())
                .thickness(THICKNESS + 0.01f * ThreadLocalRandom.current().nextFloat())
                .build()
                .buildForItemDisplay()
        }

        private fun wiresRequired(length: Double) = ceil(length).toInt()

        private fun isObstructed(start: Location, end: Location, distance: Double) = start.world.rayTraceBlocks(
            start,
            end.toVector() - start.toVector(),
            distance,
            FluidCollisionMode.ALWAYS,
            true
        ) != null

        /**
         * Returns the number of wires needed if a wire could connect between these two locations.
         * Otherwise, returns a [ConnectionFailureReason] detailing why it couldn't connect
         */
        @JvmStatic
        fun canConnect(start: Location, end: Location): Either<Int, ConnectionFailureReason> {
            val dist = start.distance(end)
            return when {
                dist > RebarConfig.MAX_WIRE_LENGTH -> Either.Right(ConnectionFailureReason.TOO_LONG)
                isObstructed(start, end, dist) -> Either.Right(ConnectionFailureReason.OBSTRUCTION)
                else -> Either.Left(wiresRequired(dist))
            }
        }

        @Suppress("UNCHECKED_CAST")
        @JvmStatic
        val loadedWires: Collection<WireEntity> get() = EntityStorage.getByKey(KEY) as Collection<WireEntity>
    }

    enum class ConnectionFailureReason(val errorMessage: Component) {
        OBSTRUCTION(Component.translatable("rebar.message.wiring.obstructed")),
        TOO_LONG(Component.translatable("rebar.message.wiring.too_long", RebarArgument.of("blocks", RebarConfig.MAX_WIRE_LENGTH)))
    }
}

private val Either<Player, Port>.location
    get() = when (this) {
        is Either.Left -> value.eyeLocation.subtract(0.0, 0.5, 0.0)
        is Either.Right -> value.second
    }