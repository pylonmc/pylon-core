package io.github.pylonmc.pylon.core.block

import com.destroystokyo.paper.event.block.BeaconEffectEvent
import io.github.pylonmc.pylon.core.block.base.*
import io.github.pylonmc.pylon.core.block.context.BlockBreakContext
import io.github.pylonmc.pylon.core.block.context.BlockCreateContext
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.item.base.BlockPlacer
import io.github.pylonmc.pylon.core.util.position.position
import io.papermc.paper.event.block.*
import io.papermc.paper.event.entity.EntityCompostItemEvent
import io.papermc.paper.event.player.PlayerChangeBeaconEffectEvent
import io.papermc.paper.event.player.PlayerInsertLecternBookEvent
import io.papermc.paper.event.player.PlayerLecternPageChangeEvent
import io.papermc.paper.event.player.PlayerOpenSignEvent
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.block.BellRingEvent
import org.bukkit.event.enchantment.EnchantItemEvent
import org.bukkit.event.enchantment.PrepareItemEnchantEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.inventory.BrewingStandFuelEvent
import org.bukkit.event.inventory.FurnaceBurnEvent
import org.bukkit.event.inventory.FurnaceExtractEvent
import org.bukkit.event.player.PlayerTakeLecternBookEvent
import org.bukkit.event.player.PlayerInteractEvent


/**
 * This listener listens for various events that would indicate a Pylon block either
 * being placed, removed, or moved
 *
 * It also handles components of multiblocks being placed, removed, or moved (this
 * includes vanilla blocks)
 */
@Suppress("UnstableApiUsage")
internal object BlockListener : Listener {

    @EventHandler(ignoreCancelled = true)
    private fun blockPlace(event: BlockPlaceEvent) {
        val item = event.itemInHand
        val pylonItem = PylonItem.fromStack(item)
        if (pylonItem is BlockPlacer) {
            val context = BlockCreateContext.PlayerPlace(event.player, item)
            val pylonBlock = pylonItem.doPlace(context, event.block)
            if (pylonBlock != null && event.player.gameMode != GameMode.CREATIVE) {
                event.player.inventory.getItem(event.hand).subtract()
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private fun blockRemove(event: BlockBreakEvent) {
        if (BlockStorage.isPylonBlock(event.block)) {
            BlockStorage.breakBlock(event.block, BlockBreakContext.PlayerBreak(event))
            event.isDropItems = false

            val player = event.player
            val tool = player.inventory.itemInMainHand

            tool.damage(1, player)
        }
    }

    @EventHandler(ignoreCancelled = true)
    private fun blockBurn(event: BlockBurnEvent) {
        BlockStorage.breakBlock(event.block, BlockBreakContext.Burned(event))
    }

    // TODO this might be dropping vanilla blocks in place of Pylon blocks
    @EventHandler(ignoreCancelled = true)
    private fun blockRemove(event: BlockExplodeEvent) {
        BlockStorage.breakBlock(event.block, BlockBreakContext.BlockExplosionOrigin(event))
        for (block in event.blockList()) {
            BlockStorage.breakBlock(block, BlockBreakContext.BlockExploded(event))
        }
    }

    // TODO this might be dropping vanilla blocks in place of Pylon blocks
    @EventHandler(ignoreCancelled = true)
    private fun blockRemove(event: EntityExplodeEvent) {
        val context = BlockBreakContext.EntityExploded(event);
        for (block in event.blockList()) {
            BlockStorage.breakBlock(block, context)
        }
    }

    @EventHandler(ignoreCancelled = true)
    private fun blockRemove(event: BlockFadeEvent) {
        BlockStorage.breakBlock(event.block, BlockBreakContext.Faded(event))
    }

    @EventHandler(ignoreCancelled = true)
    private fun disallowForming(event: BlockFormEvent) {
        if (BlockStorage.isPylonBlock(event.block.position)) {
            event.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    private fun disallowFromTo(event: BlockFromToEvent) {
        if (BlockStorage.isPylonBlock(event.block.position)) {
            event.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    private fun disallowMovementByPistons(event: BlockPistonExtendEvent) {
        for (block in event.blocks) {
            if (BlockStorage.isPylonBlock(block.position)) {
                event.isCancelled = true
                return
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private fun disallowMovementByPistons(event: BlockPistonRetractEvent) {
        for (block in event.blocks) {
            if (BlockStorage.isPylonBlock(block.position)) {
                event.isCancelled = true
                return
            }
        }
    }

    @EventHandler
    private fun onBeaconActivate(event: BeaconActivatedEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonBeacon) {
            pylonBlock.onActivated(event)
        }
    }

    @EventHandler
    private fun onBeaconDeactivate(event: BeaconDeactivatedEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonBeacon) {
            pylonBlock.onDeactivated(event)
        }
    }

    @EventHandler
    private fun onBeaconChangeEffect(event: PlayerChangeBeaconEffectEvent) {
        val pylonBlock = BlockStorage.get(event.beacon)
        if (pylonBlock is PylonBeacon) {
            pylonBlock.onEffectChange(event)
        }
    }

    @EventHandler
    private fun onBeaconEffectApply(event: BeaconEffectEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonBeacon) {
            pylonBlock.onEffectApply(event)
        }
    }

    @EventHandler
    private fun onBellRing(event: BellRingEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonBell) {
            pylonBlock.onRing(event)
        }
    }

    @EventHandler
    private fun onBellResonate(event: BellResonateEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonBell) {
            pylonBlock.onResonate(event)
        }
    }

    @EventHandler
    private fun onTNTIgnite(event: TNTPrimeEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonTNT) {
            pylonBlock.onIgnite(event)
        }
    }

    @EventHandler
    private fun onNotePlay(event: NotePlayEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonNoteBlock) {
            pylonBlock.onNotePlay(event)
        }
    }

    @EventHandler
    private fun onCrafterCraft(event: CrafterCraftEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonCrafter) {
            pylonBlock.onCraft(event)
        }
    }

    @EventHandler
    private fun onSpongeAbsorb(event: SpongeAbsorbEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonSponge) {
            pylonBlock.onAbsorb(event)
        }
    }

    @EventHandler
    private fun onStartCook(event: InventoryBlockStartEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonFurnace) {
            pylonBlock.onStartSmelting(event)
        } else if (pylonBlock is PylonCampfire) {
            pylonBlock.onStartCooking(event)
        } else if (pylonBlock is PylonBrewingStand) {
            pylonBlock.onStartBrewing(event)
        }
    }

    @EventHandler
    private fun onFinishCook(event: BlockCookEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonFurnace) {
            pylonBlock.onEndSmelting(event)
        } else if (pylonBlock is PylonCampfire) {
            pylonBlock.onEndCooking(event)
        } else if (pylonBlock is PylonBrewingStand) {
            pylonBlock.onEndBrewing(event)
        }
    }

    @EventHandler
    private fun onDispenseArmor(event: BlockDispenseArmorEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonDispenser) {
            pylonBlock.onDispenseArmor(event)
        }
    }

    @EventHandler
    private fun onDispenseItem(event: BlockDispenseEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonDispenser) {
            pylonBlock.onDispenseItem(event)
        }
    }

    @EventHandler
    private fun onDispenseLoot(event: BlockDispenseLootEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonDispenser) {
            pylonBlock.onDispenseLoot(event)
        }
    }

    @EventHandler
    private fun onDispenserShearSheep(event: BlockShearEntityEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonDispenser) {
            pylonBlock.onShearSheep(event)
        }
    }

    @EventHandler
    private fun onBlockGrow(event: BlockGrowEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonGrowable) {
            pylonBlock.onGrow(event)
        }
    }

    @EventHandler
    private fun onBlockFertilize(event: BlockFertilizeEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonGrowable) {
            pylonBlock.onFertilize(event)
        }
    }

    @EventHandler
    private fun onCauldronLevelChange(event: CauldronLevelChangeEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonCauldron) {
            pylonBlock.onLevelChange(event)
        }
    }

    @EventHandler
    private fun onSignChange(event: SignChangeEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonSign) {
            pylonBlock.onSignChange(event)
        }
    }

    @EventHandler
    private fun onVaultDisplayItem(event: VaultDisplayItemEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonTrialVault) {
            pylonBlock.onDisplayItem(event)
        }
    }

    @EventHandler
    private fun onLeafDecay(event: LeavesDecayEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonLeaf) {
            pylonBlock.onDecayNaturally(event)
        }
    }

    @EventHandler
    private fun onTargetHit(event: TargetHitEvent) {
        val pylonBlock = BlockStorage.get(event.hitBlock ?: return)
        if (pylonBlock is PylonTargetBlock) {
            pylonBlock.onHit(event)
        }
    }

    @EventHandler
    private fun onCompostByHopper(event: CompostItemEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonComposter) {
            pylonBlock.onCompostByHopper(event)
        }
    }

    @EventHandler
    private fun onCompostByEntity(event: EntityCompostItemEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonComposter) {
            pylonBlock.onCompostByEntity(event)
        }
    }

    @EventHandler
    private fun onShearBlock(event: PlayerShearBlockEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonShearable) {
            pylonBlock.onShear(event)
        }
    }

    @EventHandler
    private fun onLecternInsertBook(event: PlayerInsertLecternBookEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonLectern) {
            pylonBlock.onInsertBook(event)
        }
    }

    @EventHandler
    private fun onLecternRemoveBook(event: PlayerTakeLecternBookEvent) {
        val pylonBlock = BlockStorage.get(event.lectern.block)
        if (pylonBlock is PylonLectern) {
            pylonBlock.onRemoveBook(event)
        }
    }

    @EventHandler
    private fun onLecternChangePage(event: PlayerLecternPageChangeEvent) {
        val pylonBlock = BlockStorage.get(event.lectern.block)
        if (pylonBlock is PylonLectern) {
            pylonBlock.onChangePage(event)
        }
    }

    @EventHandler
    private fun onPistonExtend(event: BlockPistonExtendEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonPiston) {
            pylonBlock.onExtend(event)
        }
    }

    @EventHandler
    private fun onPistonRetract(event: BlockPistonRetractEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonPiston) {
            pylonBlock.onRetract(event)
        }
    }

    @EventHandler
    private fun onPreEnchant(event: PrepareItemEnchantEvent) {
        val pylonBlock = BlockStorage.get(event.enchantBlock)
        if (pylonBlock is PylonEnchantingTable) {
            pylonBlock.onPrepareEnchant(event)
        }
    }

    @EventHandler
    private fun onEnchant(event: EnchantItemEvent) {
        val pylonBlock = BlockStorage.get(event.enchantBlock)
        if (pylonBlock is PylonEnchantingTable) {
            pylonBlock.onEnchant(event)
        }
    }

    @EventHandler
    private fun onRedstoneCurrentChange(event: BlockRedstoneEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonRedstoneBlock) {
            pylonBlock.onCurrentChange(event)
        }
    }

    @EventHandler
    private fun onBrewingStandFuel(event: BrewingStandFuelEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonBrewingStand) {
            pylonBlock.onFuel(event)
        }
    }

    @EventHandler
    private fun onPreDispense(event: BlockPreDispenseEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonDispenser) {
            pylonBlock.onPreDispense(event)
        }
    }

    @EventHandler
    private fun onFailDispense(event: BlockFailedDispenseEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonDispenser) {
            pylonBlock.onFailDispense(event)
        }
    }

    @EventHandler
    private fun onFurnaceExtract(event: FurnaceExtractEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonFurnace) {
            pylonBlock.onExtractItem(event)
        }
    }

    @EventHandler
    private fun onFurnaceBurnFuel(event: FurnaceBurnEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is PylonFurnace) {
            pylonBlock.onFuelBurn(event)
        }
    }

    @EventHandler
    private fun onSignOpen(event: PlayerOpenSignEvent) {
        val pylonBlock = BlockStorage.get(event.sign.block)
        if (pylonBlock is PylonSign) {
            pylonBlock.onOpen(event)
        }
    }

    @EventHandler
    private fun onPlayerBlockInteract(event: PlayerInteractEvent) {
        val pylonBlock = BlockStorage.get(event.clickedBlock ?: return)
        if (pylonBlock is PylonInteractableBlock) {
            pylonBlock.onInteract(event)
        }
    }
}