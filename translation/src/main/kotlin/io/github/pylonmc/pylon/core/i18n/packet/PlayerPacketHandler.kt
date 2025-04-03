package io.github.pylonmc.pylon.core.i18n.packet

import io.github.pylonmc.pylon.core.i18n.PlayerTranslationHandler
import io.github.pylonmc.pylon.core.item.PylonItem
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import org.bukkit.craftbukkit.inventory.CraftItemStack

// Much inspiration has been taken from https://github.com/GuizhanCraft/SlimefunTranslation
// with permission from the author
class PlayerPacketHandler(player: ServerPlayer, private val handler: PlayerTranslationHandler) {

    private val channel = player.connection.connection.channel

    fun register() {
        channel.pipeline().addBefore("packet_handler", HANDLER_NAME, PacketHandler())
    }

    fun unregister() {
        channel.eventLoop().submit {
            channel.pipeline().remove(HANDLER_NAME)
        }
    }

    private inner class PacketHandler : ChannelDuplexHandler() {
        override fun write(ctx: ChannelHandlerContext, packet: Any, promise: ChannelPromise) {
            when (packet) {
                is ClientboundContainerSetContentPacket -> {
                    packet.items.forEach(::handleItem)
                    handleItem(packet.carriedItem)
                }
                is ClientboundContainerSetSlotPacket -> handleItem(packet.item)
            }
            super.write(ctx, packet, promise)
        }
    }

    private fun handleItem(item: ItemStack) {
        handler.handleItem(PylonItem.fromStack(CraftItemStack.asCraftMirror(item)) ?: return)
    }

    companion object {
        private const val HANDLER_NAME = "pylon_packet_handler"
    }
}