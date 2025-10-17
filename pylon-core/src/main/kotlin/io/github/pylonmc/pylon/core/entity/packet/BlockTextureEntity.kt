package io.github.pylonmc.pylon.core.entity.packet

import com.github.retrooper.packetevents.protocol.entity.data.EntityData
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes
import com.github.retrooper.packetevents.util.Vector3f
import com.github.retrooper.packetevents.wrapper.PacketWrapper
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata
import com.github.shynixn.mccoroutine.bukkit.ticks
import io.github.pylonmc.pylon.core.block.PylonBlock
import io.github.pylonmc.pylon.core.config.PylonConfig
import io.github.pylonmc.pylon.core.resourcepack.block.BlockTextureEngine
import me.tofaa.entitylib.EntityLib
import me.tofaa.entitylib.wrapper.WrapperEntity
import org.bukkit.Bukkit
import org.bukkit.util.Vector
import java.util.UUID
import kotlin.math.min

/**
 * A specific [WrapperEntity] for [PylonBlock] textures. It is an item display entity
 * that will scale based on the distance to the viewer to prevent z-fighting with the
 * block overlaid upon.
 * 
 * (see [PylonBlock.blockTextureEntity] and [BlockTextureEngine])
 */
class BlockTextureEntity(
    val block: PylonBlock
) : WrapperEntity(EntityTypes.ITEM_DISPLAY) {
    val shouldAutoUpdate = PylonConfig.BlockTextureConfig.stateUpdateInterval.ticks > 0 && block.shouldAutoRefreshBlockTextureItem()
    /**
     * Randomize the initial last update time so that not all entities update on the same tick.
     */
    var nextUpdate: Long = System.currentTimeMillis() + (Math.random() * PylonConfig.BlockTextureConfig.stateUpdateInterval.ticks).toLong()

    override fun addViewer(viewer: UUID?) {
        addOrRefreshViewer(viewer, null)
    }

    fun addOrRefreshViewer(viewer: UUID?, distanceSquared: Double?) {
        viewer!!
        if (shouldAutoUpdate && nextUpdate < System.currentTimeMillis()) {
            block.refreshBlockTextureItem()
            nextUpdate = System.currentTimeMillis() + PylonConfig.BlockTextureConfig.stateUpdateInterval.ticks
        }

        if (this.viewers.add(viewer)) {
            if (location != null && isSpawned) {
                sendPacketToViewer(viewer, this.createSpawnPacket());
                sendPacketToViewer(viewer, this.entityMeta.createPacket(), distanceSquared);
            }
        } else {
            refreshViewer(viewer, distanceSquared);
        }
    }

    fun refreshViewer(viewer: UUID, distanceSquared: Double? = null) {
        sendPacketToViewer(viewer, entityMeta.createPacket(), distanceSquared);
    }

    override fun sendPacketToViewers(packet: PacketWrapper<*>?) {
        packet!!
        this.viewers.forEach { viewer -> sendPacketToViewer(viewer, packet) }
    }

    fun sendPacketToViewer(viewer: UUID, wrapper: PacketWrapper<*>, distanceSquared: Double? = null) {
        var packet = wrapper
        if (packet is WrapperPlayServerEntityMetadata) {
            val distanceFactor = if (distanceSquared != null) {
                min(distanceSquared, 1600.0)
            } else {
                min(Bukkit.getPlayer(viewer)?.location?.toVector()?.distanceSquared(Vector(x, y, z)) ?: 0.0, 1600.0)
            }
            val scaleIncrease = (distanceFactor * 0.0005 / 20.0).toFloat()
            val metadata = ArrayList(packet.entityMetadata)
            var scale = metadata.find { it.index == SCALE_INDEX && it.type == EntityDataTypes.VECTOR3F } ?: return
            val index = metadata.indexOf(scale)
            scale = EntityData(SCALE_INDEX, EntityDataTypes.VECTOR3F, (scale.value as Vector3f).add(scaleIncrease, scaleIncrease, scaleIncrease))
            metadata[index] = scale
            packet = WrapperPlayServerEntityMetadata(packet.entityId, metadata)
        }

        val protocolManager = EntityLib.getOptionalApi().orElse(null)?.packetEvents?.protocolManager ?: return
        val channel = protocolManager.getChannel(viewer) ?: return
        protocolManager.sendPacket(channel, packet)
    }

    private companion object {
        const val SCALE_INDEX = 12
    }
}