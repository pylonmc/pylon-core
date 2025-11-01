package io.github.pylonmc.pylon.core.block

import com.destroystokyo.paper.event.block.BeaconEffectEvent
import com.destroystokyo.paper.event.block.BlockDestroyEvent
import com.destroystokyo.paper.event.player.PlayerJumpEvent
import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.block.base.*
import io.github.pylonmc.pylon.core.block.context.BlockBreakContext
import io.github.pylonmc.pylon.core.block.context.BlockCreateContext
import io.github.pylonmc.pylon.core.config.PylonConfig
import io.github.pylonmc.pylon.core.event.PylonBlockUnloadEvent
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.item.research.Research.Companion.canUse
import io.github.pylonmc.pylon.core.util.isFakeEvent
import io.papermc.paper.command.brigadier.argument.ArgumentTypes.player
import io.papermc.paper.event.block.*
import io.papermc.paper.event.entity.EntityCompostItemEvent
import io.papermc.paper.event.player.*
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.block.Container
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.block.BellRingEvent
import org.bukkit.event.enchantment.EnchantItemEvent
import org.bukkit.event.enchantment.PrepareItemEnchantEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.inventory.BrewingStandFuelEvent
import org.bukkit.event.inventory.FurnaceBurnEvent
import org.bukkit.event.inventory.FurnaceExtractEvent
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.player.PlayerBucketEmptyEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerTakeLecternBookEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.event.world.StructureGrowEvent
import org.bukkit.inventory.BlockInventoryHolder
import org.bukkit.inventory.EquipmentSlot
import java.util.*


/**
 * This listener listens for various events that would indicate a Pylon block either
 * being placed, removed, or moved
 *
 * It also handles components of multiblocks being placed, removed, or moved (this
 * includes vanilla blocks)
 */
@Suppress("UnstableApiUsage")
internal object BlockListener : Listener {
    private val blockErrMap: MutableMap<PylonBlock, Int> = WeakHashMap()

    @EventHandler(ignoreCancelled = true)
    private fun blockPlace(event: BlockPlaceEvent) {
        val item = event.itemInHand
        val player = event.player

        if (!item.type.isBlock) {
            return
        }

        val pylonItem = PylonItem.fromStack(item) ?: return
        if (!event.player.canUse(pylonItem, true)) {
            event.isCancelled = true
            return
        }

        if (isFakeEvent(event)) {
            // Fake events are for checking permissions, no need to do anything but check permission.
            if (pylonItem.schema.pylonBlockKey == null
                || BlockStorage.isPylonBlock(event.block)
            ) {
                event.isCancelled = true
                return
            }
        }

        val pylonBlock = pylonItem.place(BlockCreateContext.PlayerPlace(player, item, event))

        if (pylonBlock == null) {
            event.isCancelled = true
        }

        if (pylonBlock != null && player.gameMode != GameMode.CREATIVE) {
            player.inventory.getItem(event.hand).subtract()
        }
    }

    @EventHandler(ignoreCancelled = true)
    private fun blockRemove(event: BlockBreakEvent) {
        if (BlockStorage.isPylonBlock(event.block)) {
            if (BlockStorage.breakBlock(event.block, BlockBreakContext.PlayerBreak(event)) == null) {
                event.isCancelled = true
                return
            }
            event.isDropItems = false
            event.expToDrop = 0
            // Paper's javadocs lie currently, the damageItemStack method does not
            // respect gamemode, so we have to check it ourselves
            if (event.player.gameMode != GameMode.CREATIVE) {
                event.player.damageItemStack(EquipmentSlot.HAND, 1)
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private fun blockBurn(event: BlockBurnEvent) {
        if (BlockStorage.isPylonBlock(event.block)) {
            if (BlockStorage.breakBlock(event.block, BlockBreakContext.Burned(event)) == null) {
                event.isCancelled = true
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private fun blockRemove(event: BlockExplodeEvent) {
        if (BlockStorage.isPylonBlock(event.block) && BlockStorage.breakBlock(event.block, BlockBreakContext.BlockExplosionOrigin(event)) == null) {
            event.isCancelled = true
            return
        }

        val it = event.blockList().iterator()
        while (it.hasNext()) {
            val block = it.next()
            if (BlockStorage.isPylonBlock(block) && BlockStorage.breakBlock(block, BlockBreakContext.BlockExploded(event)) == null) {
                it.remove()
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private fun blockRemove(event: EntityExplodeEvent) {
        val it = event.blockList().iterator()
        while (it.hasNext()) {
            val block = it.next()
            if (BlockStorage.isPylonBlock(block) && BlockStorage.breakBlock(block, BlockBreakContext.EntityExploded(block, event)) == null) {
                it.remove()
            }
        }
    }

    // Event added by paper, not really documented when it's called so two separate handlers might
    // fire for some block breaks but this shouldn't be an issue
    // Primarily added to handle sensitive blocks
    @EventHandler(ignoreCancelled = true)
    private fun blockRemove(event: BlockDestroyEvent) {
        if (BlockStorage.isPylonBlock(event.block)) {
            BlockStorage.breakBlock(event.block, BlockBreakContext.Destroyed(event))
            event.setWillDrop(false)
        }
    }

    @EventHandler(ignoreCancelled = true)
    private fun blockRemove(event: BlockFadeEvent) {
        if (BlockStorage.isPylonBlock(event.block)) {
            if (BlockStorage.breakBlock(event.block, BlockBreakContext.Faded(event)) == null) {
                event.isCancelled = true
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private fun disallowForming(event: BlockFormEvent) {
        if (BlockStorage.isPylonBlock(event.block)) {
            event.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    private fun disallowFromTo(event: BlockFromToEvent) {
        if (BlockStorage.isPylonBlock(event.toBlock)) {
            event.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    private fun disallowMovementByPistons(event: BlockPistonExtendEvent) {
        for (block in event.blocks) {
            if (BlockStorage.isPylonBlock(block)) {
                event.isCancelled = true
                return
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private fun disallowMovementByPistons(event: BlockPistonRetractEvent) {
        for (block in event.blocks) {
            if (BlockStorage.isPylonBlock(block)) {
                event.isCancelled = true
                return
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private fun disallowStructureGrow(event: StructureGrowEvent) {
        for (state in event.blocks) {
            if (BlockStorage.isPylonBlock(state.block)) {
                event.isCancelled = true
                return
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    private fun preventReplacingStructureVoids(event: BlockPlaceEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock != null && pylonBlock.schema.material == Material.STRUCTURE_VOID) {
            event.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    private fun onFluidPlace(event: PlayerBucketEmptyEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock != null && pylonBlock.schema.material == Material.STRUCTURE_VOID) {
            event.isCancelled = true
        }
    }

    @EventHandler
    private fun onBeaconActivate(event: BeaconActivatedEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonBeacon) {
            try {
                pylonBlock.onActivated(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonBlock)
            }
        }
    }

    @EventHandler
    private fun onBeaconDeactivate(event: BeaconDeactivatedEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonBeacon) {
            try {
                pylonBlock.onDeactivated(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonBlock)
            }
        }
    }

    @EventHandler
    private fun onBeaconChangeEffect(event: PlayerChangeBeaconEffectEvent) {
        val pylonBlock = BlockStorage.get(event.beacon)
        if (pylonBlock is PylonBeacon) {
            try {
                pylonBlock.onEffectChange(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonBlock)
            }
        }
    }

    @EventHandler
    private fun onBeaconEffectApply(event: BeaconEffectEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonBeacon) {
            try {
                pylonBlock.onEffectApply(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonBlock)
            }
        }
    }

    @EventHandler
    private fun onBellRing(event: BellRingEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonBell) {
            try {
                pylonBlock.onRing(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonBlock)
            }
        }
    }

    @EventHandler
    private fun onBellResonate(event: BellResonateEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonBell) {
            try {
                pylonBlock.onResonate(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonBlock)
            }
        }
    }

    @EventHandler
    private fun onTNTIgnite(event: TNTPrimeEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonTNT) {
            try {
                pylonBlock.onIgnite(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonBlock)
            }
        }
    }

    @EventHandler
    private fun onNotePlay(event: NotePlayEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonNoteBlock) {
            try {
                pylonBlock.onNotePlay(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonBlock)
            }
        }
    }

    @EventHandler
    private fun onCrafterCraft(event: CrafterCraftEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonCrafter) {
            try {
                pylonBlock.onCraft(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonBlock)
            }
        }
    }

    @EventHandler
    private fun onSpongeAbsorb(event: SpongeAbsorbEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonSponge) {
            try {
                pylonBlock.onAbsorb(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonBlock)
            }
        }
    }

    @EventHandler
    private fun onStartCook(event: InventoryBlockStartEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonFurnace) {
            try {
                pylonBlock.onStartSmelting(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonBlock)
            }
        } else if (pylonBlock is PylonCampfire) {
            try {
                pylonBlock.onStartCooking(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonBlock)
            }
        } else if (pylonBlock is PylonBrewingStand) {
            try {
                pylonBlock.onStartBrewing(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonBlock)
            }
        }
    }

    @EventHandler
    private fun onFinishCook(event: BlockCookEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonFurnace) {
            try {
                pylonBlock.onEndSmelting(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonBlock)
            }
        } else if (pylonBlock is PylonCampfire) {
            try {
                pylonBlock.onEndCooking(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonBlock)
            }
        } else if (pylonBlock is PylonBrewingStand) {
            try {
                pylonBlock.onEndBrewing(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonBlock)
            }
        }
    }

    @EventHandler
    private fun onDispenseArmor(event: BlockDispenseArmorEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonDispenser) {
            try {
                pylonBlock.onDispenseArmor(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonBlock)
            }
        }
    }

    @EventHandler
    private fun onDispenseItem(event: BlockDispenseEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonDispenser) {
            try {
                pylonBlock.onDispenseItem(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonBlock)
            }
        }
    }

    @EventHandler
    private fun onDispenseLoot(event: BlockDispenseLootEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonDispenser) {
            try {
                pylonBlock.onDispenseLoot(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonBlock)
            }
        }
    }

    @EventHandler
    private fun onDispenserShearSheep(event: BlockShearEntityEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonDispenser) {
            try {
                pylonBlock.onShearSheep(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonBlock)
            }
        }
    }

    @EventHandler
    private fun onBlockGrow(event: BlockGrowEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonGrowable) {
            try {
                pylonBlock.onGrow(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonBlock)
            }
        }
    }

    @EventHandler
    private fun onBlockFertilize(event: BlockFertilizeEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonGrowable) {
            try {
                pylonBlock.onFertilize(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonBlock)
            }
        }
    }

    @EventHandler
    private fun onCauldronLevelChange(event: CauldronLevelChangeEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonCauldron) {
            try {
                pylonBlock.onLevelChange(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonBlock)
            }
        }
    }

    @EventHandler
    private fun onSignChange(event: SignChangeEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonSign) {
            try {
                pylonBlock.onSignChange(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonBlock)
            }
        }
    }

    @EventHandler
    private fun onVaultDisplayItem(event: VaultDisplayItemEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonTrialVault) {
            try {
                pylonBlock.onDisplayItem(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonBlock)
            }
        }
    }

    @EventHandler
    private fun onLeafDecay(event: LeavesDecayEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonLeaf) {
            try {
                pylonBlock.onDecayNaturally(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonBlock)
            }
        }
    }

    @EventHandler
    private fun onTargetHit(event: TargetHitEvent) {
        val pylonBlock = BlockStorage.get(event.hitBlock ?: return)
        if (pylonBlock is PylonTargetBlock) {
            try {
                pylonBlock.onHit(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonBlock)
            }
        }
    }

    @EventHandler
    private fun onCompostByHopper(event: CompostItemEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonComposter) {
            try {
                pylonBlock.onCompostByHopper(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonBlock)
            }
        }
    }

    @EventHandler
    private fun onCompostByEntity(event: EntityCompostItemEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonComposter) {
            try {
                pylonBlock.onCompostByEntity(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonBlock)
            }
        }
    }

    @EventHandler
    private fun onShearBlock(event: PlayerShearBlockEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonShearable) {
            try {
                pylonBlock.onShear(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonBlock)
            }
        }
    }

    @EventHandler
    private fun onLecternInsertBook(event: PlayerInsertLecternBookEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonLectern) {
            try {
                pylonBlock.onInsertBook(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonBlock)
            }
        }
    }

    @EventHandler
    private fun onLecternRemoveBook(event: PlayerTakeLecternBookEvent) {
        val pylonBlock = BlockStorage.get(event.lectern.block)
        if (pylonBlock is PylonLectern) {
            try {
                pylonBlock.onRemoveBook(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonBlock)
            }
        }
    }

    @EventHandler
    private fun onLecternChangePage(event: PlayerLecternPageChangeEvent) {
        val pylonBlock = BlockStorage.get(event.lectern.block)
        if (pylonBlock is PylonLectern) {
            try {
                pylonBlock.onChangePage(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonBlock)
            }
        }
    }

    @EventHandler
    private fun onPistonExtend(event: BlockPistonExtendEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonPiston) {
            try {
                pylonBlock.onExtend(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonBlock)
            }
        }
    }

    @EventHandler
    private fun onPistonRetract(event: BlockPistonRetractEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonPiston) {
            try {
                pylonBlock.onRetract(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonBlock)
            }
        }
    }

    @EventHandler
    private fun onPreEnchant(event: PrepareItemEnchantEvent) {
        val pylonBlock = BlockStorage.get(event.enchantBlock)
        if (pylonBlock is PylonEnchantingTable) {
            try {
                pylonBlock.onPrepareEnchant(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonBlock)
            }
        }
    }

    @EventHandler
    private fun onEnchant(event: EnchantItemEvent) {
        val pylonBlock = BlockStorage.get(event.enchantBlock)
        if (pylonBlock is PylonEnchantingTable) {
            try {
                pylonBlock.onEnchant(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonBlock)
            }
        }
    }

    @EventHandler
    private fun onRedstoneCurrentChange(event: BlockRedstoneEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonRedstoneBlock) {
            try {
                pylonBlock.onCurrentChange(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonBlock)
            }
        }
    }

    @EventHandler
    private fun onBrewingStandFuel(event: BrewingStandFuelEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonBrewingStand) {
            try {
                pylonBlock.onFuel(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonBlock)
            }
        }
    }

    @EventHandler
    private fun onPreDispense(event: BlockPreDispenseEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonDispenser) {
            try {
                pylonBlock.onPreDispense(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonBlock)
            }
        }
    }

    @EventHandler
    private fun onFailDispense(event: BlockFailedDispenseEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonDispenser) {
            try {
                pylonBlock.onFailDispense(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonBlock)
            }
        }
    }

    @EventHandler
    private fun onFurnaceExtract(event: FurnaceExtractEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonFurnace) {
            try {
                pylonBlock.onExtractItem(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonBlock)
            }
        }
    }

    @EventHandler
    private fun onFurnaceBurnFuel(event: FurnaceBurnEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonFurnace) {
            try {
                pylonBlock.onFuelBurn(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonBlock)
            }
        }
    }

    @EventHandler
    private fun onSignOpen(event: PlayerOpenSignEvent) {
        val pylonBlock = BlockStorage.get(event.sign.block)
        if (pylonBlock is PylonSign) {
            try {
                pylonBlock.onOpen(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonBlock)
            }
        }
    }

    @EventHandler
    private fun onPlayerBlockInteract(event: PlayerInteractEvent) {
        val pylonBlock = BlockStorage.get(event.clickedBlock ?: return)
        if (pylonBlock is PylonInteractBlock) {
            try {
                pylonBlock.onInteract(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonBlock)
            }
        }
    }

    @EventHandler
    private fun onPlayerToggleSneak(event: PlayerToggleSneakEvent) {
        val blockUnder = event.player.location.add(0.0, -1.0, 0.0).block
        val blockIn = event.player.location.add(0.0, 0.0, 0.0).block
        val pylonBlock = BlockStorage.get(blockUnder) ?: BlockStorage.get(blockIn)
        if (pylonBlock is PylonSneakableBlock) {
            /*
            * Event player is from before the event is triggered, so when the player
            * is marked as *not* sneaking, they just toggled it.
            */
            if (!event.player.isSneaking) {
                try {
                    pylonBlock.onSneakedOn(event)
                } catch (e: Exception) {
                    logEventHandleErr(event, e, pylonBlock)
                }
            } else {
                try {
                    pylonBlock.onUnsneakedOn(event)
                } catch (e: Exception) {
                    logEventHandleErr(event, e, pylonBlock)
                }
            }
        }
    }

    @EventHandler
    private fun onPlayerJumpEvent(event: PlayerJumpEvent) {
        val blockUnder = event.player.location.add(0.0, -1.0, 0.0).block
        val blockIn = event.player.location.add(0.0, 0.0, 0.0).block
        val pylonBlock = BlockStorage.get(blockUnder) ?: BlockStorage.get(blockIn)
        if (pylonBlock is PylonJumpBlock) {
            try {
                pylonBlock.onJumpedOn(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonBlock)
            }
        }
    }

    @EventHandler
    private fun onUnload(event: PylonBlockUnloadEvent) {
        if (event.pylonBlock is PylonUnloadBlock) {
            try {
                event.pylonBlock.onUnload(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, event.pylonBlock)
            }
        }
    }

    @EventHandler
    private fun onFlowerpotManipulate(event: PlayerFlowerPotManipulateEvent) {
        val block = BlockStorage.get(event.flowerpot)
        if (block is PylonFlowerPot) {
            try {
                block.onFlowerPotManipulated(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, block)
            }
        }
    }

    @EventHandler
    private fun onInventoryOpen(event: InventoryOpenEvent) {
        val holder = event.inventory.holder
        if (holder is Container) {
            val block = BlockStorage.get(holder.block)
            if (block is PylonVanillaContainerBlock) {
                block.onInventoryOpen(event)
            }
        }
    }

    @EventHandler
    private fun onItemMove(event: InventoryMoveItemEvent) {
        val sourceHolder = event.source.holder
        if (sourceHolder is BlockInventoryHolder) {
            val sourceBlock = BlockStorage.get(sourceHolder.block)
            if (sourceBlock is PylonVanillaContainerBlock) {
                sourceBlock.onItemMoveFrom(event)
            }
        }
        val destHolder = event.destination.holder
        if (destHolder is BlockInventoryHolder) {
            val destBlock = BlockStorage.get(destHolder.block)
            if (destBlock is PylonVanillaContainerBlock) {
                destBlock.onItemMoveTo(event)
            }
        }
    }

    @JvmSynthetic
    internal fun logEventHandleErr(event: Event?, e: Exception, block: PylonBlock) {
        if (event != null) {
            PylonCore.logger.severe("Error when handling block(${block.key}, ${block.block.location}) event handler ${event.javaClass.simpleName}: ${e.localizedMessage}")
        } else {
            PylonCore.logger.severe("Error when handling block(${block.key}, ${block.block.location}) ticking: ${e.localizedMessage}")
        }
        e.printStackTrace()
        blockErrMap[block] = blockErrMap[block]?.plus(1) ?: 1
        if (blockErrMap[block]!! > PylonConfig.allowedBlockErrors) {
            BlockStorage.makePhantom(block)
            TickManager.stopTicking(block)
        }
    }
}