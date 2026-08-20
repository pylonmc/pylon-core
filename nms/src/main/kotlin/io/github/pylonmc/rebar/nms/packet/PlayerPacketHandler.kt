@file:Suppress("UnstableApiUsage")

package io.github.pylonmc.rebar.nms.packet

import io.github.pylonmc.rebar.Rebar
import io.github.pylonmc.rebar.culling.BlockCullingEngine
import io.github.pylonmc.rebar.i18n.PlayerTranslationHandler
import io.github.pylonmc.rebar.nms.entity.BlockTextureEntityImpl
import io.github.pylonmc.rebar.util.position.BlockPosition
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import net.minecraft.core.component.DataComponentExactPredicate
import net.minecraft.core.component.DataComponentMap
import net.minecraft.core.component.PatchedDataComponentMap
import net.minecraft.network.HashedPatchMap
import net.minecraft.network.HashedStack
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.*
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.HashOps
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.ItemStackTemplate
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.display.*
import net.minecraft.world.item.trading.ItemCost
import net.minecraft.world.item.trading.MerchantOffer
import net.minecraft.world.item.trading.MerchantOffers
import org.bukkit.craftbukkit.inventory.CraftItemStack
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level


// Much inspiration has been taken from https://github.com/GuizhanCraft/SlimefunTranslation
// with permission from the author
class PlayerPacketHandler(private val player: ServerPlayer, val handler: PlayerTranslationHandler) {

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
        val pipeline = channel.pipeline()
        if (pipeline.get(HANDLER_NAME) == null) {
            pipeline.addBefore("packet_handler", HANDLER_NAME, PacketHandler())
        }
    }

    fun unregister() {
        channel.eventLoop().submit {
            val pipeline = channel.pipeline()
            if (pipeline.get(HANDLER_NAME) != null) {
                pipeline.remove(HANDLER_NAME)
            }
        }
    }

    private inner class PacketHandler : ChannelDuplexHandler() {
        override fun write(ctx: ChannelHandlerContext, packet: Any, promise: ChannelPromise) {
            @Suppress("UNCHECKED_CAST")
            val typedPacket = packet as? Packet<in ClientGamePacketListener>
                ?: return super.write(ctx, packet, promise)
            super.write(ctx, handleOutgoingPacketSafely(typedPacket), promise)
        }

        override fun channelRead(ctx: ChannelHandlerContext, packet: Any) {
            @Suppress("UNCHECKED_CAST")
            val typedPacket = packet as? Packet<in ServerGamePacketListener>
                ?: return super.channelRead(ctx, packet)
            super.channelRead(ctx, handleIncomingPacketSafely(typedPacket))
        }
    }

    /**
     * Rebar is intentionally fail-open at the Netty boundary. A Minecraft update can change an
     * internal packet implementation without changing the public Paper API; if one translation
     * path breaks, vanilla inventory, merchant, and recipe packets must still reach the client.
     */
    private fun handleOutgoingPacketSafely(packet: Packet<in ClientGamePacketListener>): Packet<in ClientGamePacketListener> {
        return try {
            handleOutgoingPacket(packet)
        } catch (e: Exception) {
            reportPacketFailure("outgoing", packet, e)
            packet
        } catch (e: LinkageError) {
            reportPacketFailure("outgoing", packet, e)
            packet
        }
    }

    private fun handleIncomingPacketSafely(packet: Packet<in ServerGamePacketListener>): Packet<in ServerGamePacketListener> {
        return try {
            handleIncomingPacket(packet)
        } catch (e: Exception) {
            reportPacketFailure("incoming", packet, e)
            packet
        } catch (e: LinkageError) {
            reportPacketFailure("incoming", packet, e)
            packet
        }
    }

    private fun reportPacketFailure(direction: String, packet: Any, throwable: Throwable) {
        val key = "$direction:${packet.javaClass.name}"
        if (!REPORTED_PACKET_FAILURES.add(key)) return

        Rebar.logger.log(
            Level.WARNING,
            "Failed to translate $direction ${packet.javaClass.simpleName}; " +
                "the original packet will be passed through. This packet type will only be logged once.",
            throwable
        )
    }

    private fun handleOutgoingPacket(packet: Packet<in ClientGamePacketListener>): Packet<in ClientGamePacketListener> =
        when (packet) {
            is ClientboundBundlePacket -> ClientboundBundlePacket(packet.subPackets().map(::handleOutgoingPacketSafely))

            is ClientboundContainerSetContentPacket -> packet.apply {
                items.forEach(::translate)
                translate(packet.carriedItem)
            }

            is ClientboundContainerSetSlotPacket -> packet.apply { translate(item) }
            is ClientboundSetCursorItemPacket -> packet.apply { translate(contents) }
            is ClientboundRecipeBookAddPacket -> ClientboundRecipeBookAddPacket(
                packet.entries.map {
                    ClientboundRecipeBookAddPacket.Entry(
                        RecipeDisplayEntry(
                            it.contents.id,
                            handleRecipeDisplay(it.contents.display),
                            it.contents.group,
                            it.contents.category,
                            it.contents.craftingRequirements.map { ingredients ->
                                ingredients.map { ingredient ->
                                    ingredient.itemStacks()?.let { stacks ->
                                        Ingredient.ofStacks(stacks.map { item -> translate(item.copy()) })
                                    } ?: ingredient
                                }
                            }
                        ),
                        it.flags
                    )
                },
                packet.replace
            )

            is ClientboundMerchantOffersPacket -> ClientboundMerchantOffersPacket(
                packet.containerId,
                MerchantOffers().apply { addAll(packet.offers.map { offer ->
                    MerchantOffer(
                        translate(offer.getItemCostA()),
                        offer.getItemCostB().map(::translate),
                        translate(offer.getResult().copy()),
                        offer.getUses(),
                        offer.getMaxUses(),
                        offer.getXp(),
                        offer.getPriceMultiplier(),
                        offer.getDemand()
                    ).apply {
                        rewardExp = offer.shouldRewardExp()
                        setSpecialPriceDiff(offer.getSpecialPriceDiff())
                        ignoreDiscounts = offer.ignoreDiscounts
                    }
                }) },
                packet.villagerLevel,
                packet.villagerXp,
                packet.showProgress(),
                packet.canRestock()
            )

            is ClientboundPlaceGhostRecipePacket -> ClientboundPlaceGhostRecipePacket(
                packet.containerId,
                handleRecipeDisplay(packet.recipeDisplay)
            )

            is ClientboundSetEntityDataPacket -> packet.let {
                val translated = mutableMapOf<Int, SynchedEntityData.DataValue<*>>()
                it.packedItems.forEachIndexed { i, item ->
                    val value = item.value
                    if (value is ItemStack) {
                        val copy = value.copy()
                        translate(copy)
                        translated[i] = SynchedEntityData.DataValue(item.id, EntityDataSerializers.ITEM_STACK, copy)
                    }
                }

                if (translated.isEmpty()) it
                else ClientboundSetEntityDataPacket(
                    it.id,
                    it.packedItems.mapIndexed { i, item ->
                        translated[i] ?: item
                    }
                )
            }

            is ClientboundSetEquipmentPacket -> packet.apply {
                slots.forEach { slot ->
                    translate(slot.second)
                }
            }

            is ClientboundBlockUpdatePacket -> packet.let {
                val cullingJob = BlockCullingEngine.getCullingJob(player.uuid) ?: return@let it
                val packedPos = BlockPosition.asLong(it.pos.x, it.pos.y, it.pos.z)

                cullingJob.lightDelegates[packedPos]?.let { delegates ->
                    for (delegate in delegates) {
                        if (!delegate.disableBlockTextureEntity && delegate.blockTextureEntity is BlockTextureEntityImpl) {
                            (delegate.blockTextureEntity as BlockTextureEntityImpl).tryUpdateLighting()
                        }
                    }
                }

                return@let cullingJob.visible[packedPos]?.let { block ->
                    if (!block.disableBlockTextureEntity && block.blockTextureEntity is BlockTextureEntityImpl) {
                        val entity = block.blockTextureEntity as BlockTextureEntityImpl
                        return@let if (entity.tryUpdateState()) ClientboundBundlePacket(mutableListOf(entity.itemUpdatePacket, it)) else it
                    }
                    return@let it
                } ?: it
            }

            is ClientboundSectionBlocksUpdatePacket -> packet.let {
                val cullingJob = BlockCullingEngine.getCullingJob(player.uuid) ?: return@let it
                val packets = mutableListOf<Packet<in ClientGamePacketListener>>()
                it.runUpdates { pos, _ ->
                    val packedPos = BlockPosition.asLong(pos.x, pos.y, pos.z)

                    cullingJob.lightDelegates[packedPos]?.let { delegates ->
                        for (delegate in delegates) {
                            if (!delegate.disableBlockTextureEntity && delegate.blockTextureEntity is BlockTextureEntityImpl) {
                                (delegate.blockTextureEntity as BlockTextureEntityImpl).tryUpdateLighting()
                            }
                        }
                    }

                    cullingJob.visible[packedPos]?.let { block ->
                        if (!block.disableBlockTextureEntity && block.blockTextureEntity is BlockTextureEntityImpl) {
                            val entity = block.blockTextureEntity as BlockTextureEntityImpl
                            if (entity.tryUpdateState()) {
                                packets.add(entity.itemUpdatePacket)
                            }
                        }
                    }
                }
                return@let if (packets.isEmpty()) it else ClientboundBundlePacket(packets.apply { add(it) })
            }

            else -> packet
        }

    private fun handleIncomingPacket(packet: Packet<in ServerGamePacketListener>): Packet<in ServerGamePacketListener> =
        when (packet) {
            is ServerboundContainerClickPacket -> ServerboundContainerClickPacket(
                packet.containerId,
                packet.stateId,
                packet.slotNum,
                packet.buttonNum,
                packet.containerInput,
                packet.changedSlots,
                if (packet.changedSlots.size == 1) {
                    val slot = packet.changedSlots.keys.single()
                    val menu = player.containerMenu
                    if (menu.isValidSlotIndex(slot)) {
                        HashedStack.create(menu.getSlot(slot).item, hashGenerator)
                    } else {
                        HashedStack.EMPTY
                    }
                } else {
                    HashedStack.create(player.containerMenu.carried, hashGenerator)
                }
            )

            else -> packet
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
            else -> display
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
                ItemStackTemplate.fromNonEmptyStack(display.stack.create().apply(::translate))
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

            is SlotDisplay.OnlyWithComponent -> SlotDisplay.OnlyWithComponent(
                handleSlotDisplay(display.source),
                display.component
            )

            is SlotDisplay.WithAnyPotion -> SlotDisplay.WithAnyPotion(
                handleSlotDisplay(display.display)
            )

            is SlotDisplay.DyedSlotDemo -> SlotDisplay.DyedSlotDemo(
                handleSlotDisplay(display.dye),
                handleSlotDisplay(display.target)
            )

            else -> display
        }
    }

    private fun translate(itemCost: ItemCost): ItemCost {
        // Build a client-only cost from a translated copy. The predicate must describe the same
        // translated stack sent to the client or merchant selection/click validation can desync.
        // The live server MerchantOffer remains untouched because itemStack is copied first.
        val costStack = translate(itemCost.itemStack.copy())
        val costPredicate = DataComponentExactPredicate.allOf(
            PatchedDataComponentMap.fromPatch(DataComponentMap.EMPTY, costStack.componentsPatch)
        )
        return ItemCost(costStack.typeHolder(), costStack.count, costPredicate, costStack)
    }

    private fun translate(item: ItemStack): ItemStack {
        if (item.isEmpty) return item
        try {
            handler.handleItem(CraftItemStack.asCraftMirror(item))
        } catch (e: Throwable) {
            // Log the error nicely instead of kicking the player off
            // and causing two days of headache. True story.
            Rebar.logger.log(
                Level.SEVERE,
                "An error occurred while handling item translations",
                e
            )
        }
        return item
    }

    companion object {
        private const val HANDLER_NAME = "rebar_packet_handler"
        private val REPORTED_PACKET_FAILURES = ConcurrentHashMap.newKeySet<String>()
    }
}
