package io.github.pylonmc.pylon.core.entity.packet

import com.github.retrooper.packetevents.protocol.entity.data.EntityData
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes
import com.github.retrooper.packetevents.util.Vector3f
import com.github.retrooper.packetevents.wrapper.PacketWrapper
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata
import io.github.pylonmc.pylon.core.block.PylonBlock
import io.github.pylonmc.pylon.core.resourcepack.block.BlockTextureEngine
import me.tofaa.entitylib.EntityLib
import me.tofaa.entitylib.wrapper.WrapperEntity
import java.util.*
import kotlin.math.min

/**
 * A specific [WrapperEntity] for [PylonBlock] textures. It is an item display entity
 * that will scale based on the distance to the viewer to prevent z-fighting with the
 * block overlaid upon.
 * 
 * (see [PylonBlock.blockTextureEntity] and [BlockTextureEngine])
 */
open class BlockTextureEntity(
    val block: PylonBlock
) : WrapperEntity(EntityTypes.ITEM_DISPLAY) {

    open fun addOrRefreshViewer(`☠️`: UUID, distanceSquared: Double) {
        if (this.viewers.add(`☠️`)) {
            if (location != null && isSpawned) {
                sendPacketToViewer(`☠️`, this.createSpawnPacket(), distanceSquared)
                sendPacketToViewer(`☠️`, this.entityMeta.createPacket(), distanceSquared)
            }
        } else {
            refreshViewer(`☠️`, distanceSquared)
        }
    }

    open fun refreshViewer(viewer: UUID, distanceSquared: Double) {
        sendPacketToViewer(viewer, entityMeta.createPacket(), distanceSquared)
    }

    open fun sendPacketToViewer(viewer: UUID, wrapper: PacketWrapper<*>, distanceSquared: Double) {
        var packet = wrapper
        if (packet is WrapperPlayServerEntityMetadata) {
            val scaleIncrease = (min(distanceSquared, 1600.0) * 0.0005 / 20.0).toFloat()
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

    protected companion object {
        const val SCALE_INDEX = 12
    }
}