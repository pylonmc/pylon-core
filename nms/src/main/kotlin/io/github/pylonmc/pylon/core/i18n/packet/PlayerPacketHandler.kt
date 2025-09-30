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
import net.minecraft.network.HashedPatchMap
import net.minecraft.network.HashedStack
import net.minecraft.network.protocol.game.*
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.HashOps
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.display.*
import org.bukkit.craftbukkit.inventory.CraftItemStack
import java.util.WeakHashMap
import java.util.logging.Level
import org.bukkit.inventory.ItemStack as BukkitItemStack


// Much inspiration has been taken from https://github.com/GuizhanCraft/SlimefunTranslation
// with permission from the author
class PlayerPacketHandler(private val player: ServerPlayer, private val handler: PlayerTranslationHandler) {

    private val channel = player.connection.connection.channel

    private val hashGenerator: HashedPatchMap.HashGenerator

    init {
        // (PaperMC server) https://discord.com/channels/289587909051416579/555462289851940864/1371651093972385823
        val registryOps = player.registryAccess().createSerializationContext(HashOps.CRC32C_INSTANCE)
        hashGenerator = HashedPatchMap.HashGenerator { component ->
            component.encodeValue(registryOps)
                .getOrThrow { IllegalArgumentException("Failed to hash $component: $it") }
                .asInt()
        }
    }

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
            var packet = packet
            when (packet) {
                is ClientboundContainerSetContentPacket -> {
                    packet.items.forEach(::translateItem)
                    translateItem(packet.carriedItem)
                }

                is ClientboundContainerSetSlotPacket -> translateItem(packet.item)
                is ClientboundSetCursorItemPacket -> translateItem(packet.contents)
                is ClientboundRecipeBookAddPacket -> {
                    // This requires a full copy for some reason
                    packet = ClientboundRecipeBookAddPacket(
                        packet.entries.map {
                            ClientboundRecipeBookAddPacket.Entry(
                                RecipeDisplayEntry(
                                    it.contents.id,
                                    handleRecipeDisplay(it.contents.display),
                                    it.contents.group,
                                    it.contents.category,
                                    it.contents.craftingRequirements
                                ),
                                it.flags
                            )
                        },
                        packet.replace
                    )
                }

                is ClientboundPlaceGhostRecipePacket -> {
                    packet = ClientboundPlaceGhostRecipePacket(
                        packet.containerId,
                        handleRecipeDisplay(packet.recipeDisplay)
                    )
                }
            }
            super.write(ctx, packet, promise)
        }

        override fun channelRead(ctx: ChannelHandlerContext, packet: Any) {
            var packet = packet
            when (packet) {
                is ServerboundContainerClickPacket -> {
                    // force server to resend the item
                    packet = ServerboundContainerClickPacket(
                        packet.containerId,
                        packet.stateId,
                        packet.slotNum,
                        packet.buttonNum,
                        packet.clickType,
                        packet.changedSlots,
                        HashedStack.create(player.containerMenu.carried, hashGenerator)
                    )
                }

                is ServerboundSetCreativeModeSlotPacket -> resetItem(packet.itemStack)
            }
            super.channelRead(ctx, packet)
        }
    }

    private fun handleRecipeDisplay(display: RecipeDisplay): RecipeDisplay {
        return when (display) {
            is FurnaceRecipeDisplay -> FurnaceRecipeDisplay(
                handleSlotDisplay(display.ingredient),
                handleSlotDisplay(display.fuel),
                handleSlotDisplay(display.result),
                handleSlotDisplay(display.craftingStation),
                display.duration,
                display.experience
            )

            is ShapedCraftingRecipeDisplay -> ShapedCraftingRecipeDisplay(
                display.width,
                display.height,
                display.ingredients.map(::handleSlotDisplay),
                handleSlotDisplay(display.result),
                handleSlotDisplay(display.craftingStation)
            )

            is ShapelessCraftingRecipeDisplay -> ShapelessCraftingRecipeDisplay(
                display.ingredients.map(::handleSlotDisplay),
                handleSlotDisplay(display.result),
                handleSlotDisplay(display.craftingStation)
            )

            is SmithingRecipeDisplay -> SmithingRecipeDisplay(
                handleSlotDisplay(display.template),
                handleSlotDisplay(display.base),
                handleSlotDisplay(display.addition),
                handleSlotDisplay(display.result),
                handleSlotDisplay(display.craftingStation)
            )

            is StonecutterRecipeDisplay -> display
            else -> throw IllegalArgumentException("Unknown recipe display type: ${display::class.simpleName}")
        }
    }

    private fun handleSlotDisplay(display: SlotDisplay): SlotDisplay {
        return when (display) {
            is SlotDisplay.AnyFuel,
            is SlotDisplay.ItemSlotDisplay,
            is SlotDisplay.TagSlotDisplay,
            is SlotDisplay.Empty -> display

            is SlotDisplay.Composite -> SlotDisplay.Composite(display.contents.map(::handleSlotDisplay))
            is SlotDisplay.ItemStackSlotDisplay -> SlotDisplay.ItemStackSlotDisplay(
                display.stack.copy().apply(::translateItem)
            )

            is SlotDisplay.SmithingTrimDemoSlotDisplay -> SlotDisplay.SmithingTrimDemoSlotDisplay(
                handleSlotDisplay(display.base),
                handleSlotDisplay(display.material),
                display.pattern
            )

            is SlotDisplay.WithRemainder -> SlotDisplay.WithRemainder(
                handleSlotDisplay(display.input),
                handleSlotDisplay(display.remainder)
            )

            else -> throw IllegalArgumentException("Unknown slot display type: ${display::class.simpleName}")
        }
    }

    private inline fun handleItem(item: ItemStack, handler: (BukkitItemStack) -> Unit) {
        if (item.isEmpty) return
        try {
            handler(CraftItemStack.asCraftMirror(item))
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

private fun reset(stack: BukkitItemStack) {
    val item = PylonItem.fromStack(stack) ?: return
    val name = names.getOrPut(item.schema) {
        item.schema.itemStack.getData(DataComponentTypes.ITEM_NAME)!!
    }
    val lore = lores.getOrPut(item.schema) {
        item.schema.itemStack.getData(DataComponentTypes.LORE)!!
    }
    item.stack.setData(DataComponentTypes.ITEM_NAME, name)
    item.stack.setData(DataComponentTypes.LORE, lore)
}