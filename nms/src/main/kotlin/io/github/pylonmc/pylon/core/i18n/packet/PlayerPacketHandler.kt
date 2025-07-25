@file:Suppress("UnstableApiUsage")

package io.github.pylonmc.pylon.core.i18n.packet

import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.i18n.PlayerTranslationHandler
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.item.PylonItemSchema
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.text.Component
import net.minecraft.network.protocol.game.*
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.display.*
import org.bukkit.craftbukkit.inventory.CraftItemStack
import java.util.WeakHashMap
import java.util.logging.Level

// Much inspiration has been taken from https://github.com/GuizhanCraft/SlimefunTranslation
// with permission from the author
class PlayerPacketHandler(private val player: ServerPlayer, private val handler: PlayerTranslationHandler) {

    private val connection = player.connection
    private val channel = connection.connection.channel

    fun register() {
        channel.pipeline().addBefore("packet_handler", HANDLER_NAME, PacketHandler())
    }

    fun unregister() {
        channel.eventLoop().submit {
            channel.pipeline().remove(HANDLER_NAME)
        }
    }

    fun resendInventory() {
        val inventory = player.containerMenu
        for (slot in 0..45) {
            val item = inventory.getSlot(slot).item
            player.containerSynchronizer.sendSlotChange(inventory, slot, item)
        }
    }

    private inner class PacketHandler : ChannelDuplexHandler() {
        override fun write(ctx: ChannelHandlerContext, packet: Any, promise: ChannelPromise) {
            when (packet) {
                is ClientboundContainerSetContentPacket -> {
                    packet.items.forEach(::translateItem)
                    translateItem(packet.carriedItem)
                }

                is ClientboundContainerSetSlotPacket -> translateItem(packet.item)
                is ClientboundRecipeBookAddPacket -> packet.entries
                    .forEach { handleRecipeDisplay(it.contents.display) }
            }
            super.write(ctx, packet, promise)
        }

        override fun channelRead(ctx: ChannelHandlerContext, packet: Any) {
            when (packet) {
                is ServerboundContainerClickPacket -> resetItem(packet.carriedItem)
                is ServerboundSetCreativeModeSlotPacket -> resetItem(packet.itemStack)
            }
            super.channelRead(ctx, packet)
        }
    }

    private fun handleRecipeDisplay(display: RecipeDisplay) {
        when (display) {
            is FurnaceRecipeDisplay -> display.run {
                handleSlotDisplay(ingredient)
                handleSlotDisplay(fuel)
                handleSlotDisplay(result)
            }

            is ShapedCraftingRecipeDisplay -> display.run {
                ingredients.forEach(::handleSlotDisplay)
                handleSlotDisplay(result)
            }

            is ShapelessCraftingRecipeDisplay -> display.run {
                ingredients.forEach(::handleSlotDisplay)
                handleSlotDisplay(result)
            }

            is SmithingRecipeDisplay -> display.run {
                handleSlotDisplay(template)
                handleSlotDisplay(base)
                handleSlotDisplay(addition)
                handleSlotDisplay(result)
            }

            is StonecutterRecipeDisplay -> return // Do nothing, we don't support stonecutter recipes
            else -> throw IllegalArgumentException("Unknown recipe display type: ${display::class.simpleName}")
        }
    }

    private fun handleSlotDisplay(display: SlotDisplay) {
        when (display) {
            is SlotDisplay.AnyFuel,
            is SlotDisplay.ItemSlotDisplay,
            is SlotDisplay.TagSlotDisplay,
            is SlotDisplay.Empty -> return

            is SlotDisplay.Composite -> display.contents.forEach(::handleSlotDisplay)
            is SlotDisplay.ItemStackSlotDisplay -> translateItem(display.stack)
            is SlotDisplay.SmithingTrimDemoSlotDisplay -> display.run {
                handleSlotDisplay(base)
                handleSlotDisplay(material)
                handleSlotDisplay(pattern)
            }

            is SlotDisplay.WithRemainder -> display.run {
                handleSlotDisplay(input)
                handleSlotDisplay(remainder)
            }

            else -> throw IllegalArgumentException("Unknown slot display type: ${display::class.simpleName}")
        }
    }

    private inline fun handleItem(item: ItemStack, handler: (PylonItem) -> Unit) {
        if (item.isEmpty) return
        try {
            handler(PylonItem.fromStack(CraftItemStack.asCraftMirror(item)) ?: return)
        } catch (e: Throwable) {
            // Log the error nicely instead of kicking the player off
            // and causing two days of headache. True story.
            PylonCore.logger.log(
                Level.SEVERE,
                "An error occurred while handling item translations",
                e
            )
        }
    }

    private fun translateItem(item: ItemStack) = handleItem(item, handler::handleItem)
    private fun resetItem(item: ItemStack) = handleItem(item, ::reset)

    companion object {
        private const val HANDLER_NAME = "pylon_packet_handler"
    }
}

private val names = WeakHashMap<PylonItemSchema, Component>()
private val lores = WeakHashMap<PylonItemSchema, ItemLore>()

private fun reset(item: PylonItem) {
    val name = names.getOrPut(item.schema) {
        item.schema.itemStack.getData(DataComponentTypes.ITEM_NAME)!!
    }
    val lore = lores.getOrPut(item.schema) {
        item.schema.itemStack.getData(DataComponentTypes.LORE)!!
    }
    item.stack.setData(DataComponentTypes.ITEM_NAME, name)
    item.stack.setData(DataComponentTypes.LORE, lore)
}