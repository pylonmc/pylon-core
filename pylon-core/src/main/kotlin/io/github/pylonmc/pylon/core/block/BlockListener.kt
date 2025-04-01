package io.github.pylonmc.pylon.core.block

import com.destroystokyo.paper.event.block.BeaconEffectEvent
import io.github.pylonmc.pylon.core.block.base.*
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.persistence.blockstorage.BlockStorage
import io.github.pylonmc.pylon.core.util.position.position
import io.papermc.paper.event.block.BeaconActivatedEvent
import io.papermc.paper.event.block.BeaconDeactivatedEvent
import io.papermc.paper.event.block.CompostItemEvent
import io.papermc.paper.event.block.TargetHitEvent
import io.papermc.paper.event.entity.EntityCompostItemEvent
import io.papermc.paper.event.player.PlayerChangeBeaconEffectEvent
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.entity.EntityExplodeEvent

/**
 * This listener listens for various events that would indicate a Pylon block either
 * being placed, removed, or moved
 */
@Suppress("UnstableApiUsage")
internal object BlockListener : Listener {

    @EventHandler(ignoreCancelled = true)
    private fun blockPlace(event: BlockPlaceEvent) {
        val item = event.itemInHand
        if (PylonItem.fromStack(item) != null && item.type.isBlock) {
            event.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    private fun blockRemove(event: BlockBreakEvent) {
        breakBlock(event.block, BlockBreakContext.PlayerBreak(event))
    }

    @EventHandler(ignoreCancelled = true)
    private fun blockBurn(event: BlockBurnEvent) {
        breakBlock(event.block, BlockBreakContext.Burned(event))
    }

    @EventHandler(ignoreCancelled = true)
    private fun blockRemove(event: BlockExplodeEvent) {
        breakBlock(event.block, BlockBreakContext.Exploded(event))
        for (block in event.blockList()) {
            breakBlock(block, BlockBreakContext.WasExploded)
        }
    }

    @EventHandler(ignoreCancelled = true)
    private fun blockRemove(event: EntityExplodeEvent) {
        for (block in event.blockList()) {
            breakBlock(block, BlockBreakContext.WasExploded)
        }
    }

    @EventHandler(ignoreCancelled = true)
    private fun blockRemove(event: BlockFadeEvent) {
        breakBlock(event.block, BlockBreakContext.Faded(event))
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

    private fun breakBlock(block: Block, reason: BlockBreakContext) {
        val drops = BlockStorage.breakBlock(block, reason) ?: return
        for (drop in drops) {
            block.world.dropItemNaturally(block.location.add(0.5, 0.1, 0.5), drop)
        }
    }

    @EventHandler
    private fun onBeaconActivate(event: BeaconActivatedEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is Beacon) {
            pylonBlock.onActivated(event)
        }
    }

    @EventHandler
    private fun onBeaconDeactivate(event: BeaconDeactivatedEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is Beacon) {
            pylonBlock.onDeactivated(event)
        }
    }

    @EventHandler
    private fun onBeaconChangeEffect(event: PlayerChangeBeaconEffectEvent) {
        val pylonBlock = BlockStorage.get(event.beacon)
        if (pylonBlock is Beacon) {
            pylonBlock.onEffectChange(event)
        }
    }

    @EventHandler
    private fun onBeaconEffectApply(event: BeaconEffectEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is Beacon) {
            pylonBlock.onEffectApply(event)
        }
    }

    @EventHandler
    private fun onBellRing(event: BellRingEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is Bell) {
            pylonBlock.onRing(event)
        }
    }

    @EventHandler
    private fun onBellResonate(event: BellResonateEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is Bell) {
            pylonBlock.onResonate(event)
        }
    }

    @EventHandler
    private fun onTNTIgnite(event: TNTPrimeEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is TNT) {
            pylonBlock.onIgnite(event)
        }
    }

    @EventHandler
    private fun onNotePlay(event: NotePlayEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is NoteBlock) {
            pylonBlock.onNotePlay(event)
        }
    }

    @EventHandler
    private fun onCrafterCraft(event: CrafterCraftEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is Crafter) {
            pylonBlock.onCraft(event)
        }
    }

    @EventHandler
    private fun onSpongeAbsorb(event: SpongeAbsorbEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is Sponge) {
            pylonBlock.onAbsorb(event)
        }
    }

    @EventHandler
    private fun onStartCook(event: InventoryBlockStartEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is Furnace) {
            pylonBlock.onStartSmelting(event)
        } else if (pylonBlock is Campfire) {
            pylonBlock.onStartCooking(event)
        } else if (pylonBlock is BrewingStand) {
            pylonBlock.onStartBrewing(event)
        }
    }

    @EventHandler
    private fun onFinishCook(event: BlockCookEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is Furnace) {
            pylonBlock.onEndSmelting(event)
        } else if (pylonBlock is Campfire) {
            pylonBlock.onEndCooking(event)
        } else if (pylonBlock is BrewingStand) {
            pylonBlock.onEndBrewing(event)
        }
    }

    @EventHandler
    private fun onDispenseArmor(event: BlockDispenseArmorEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is Dispenser) {
            pylonBlock.onDispenseArmor(event)
        }
    }

    @EventHandler
    private fun onDispenseItem(event: BlockDispenseEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is Dispenser) {
            pylonBlock.onDispenseItem(event)
        }
    }

    @EventHandler
    private fun onDispenseLoot(event: BlockDispenseLootEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is Dispenser) {
            pylonBlock.onDispenseLoot(event)
        }
    }

    @EventHandler
    private fun onDispenserShearSheep(event: BlockShearEntityEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is Dispenser) {
            pylonBlock.onShearSheep(event)
        }
    }

    @EventHandler
    private fun onBlockGrow(event: BlockGrowEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is Growable) {
            pylonBlock.onGrow(event)
        }
    }

    @EventHandler
    private fun onBlockFertilize(event: BlockFertilizeEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is Growable) {
            pylonBlock.onFertilize(event)
        }
    }

    @EventHandler
    private fun onCauldronLevelChange(event: CauldronLevelChangeEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is Cauldron) {
            pylonBlock.onLevelChange(event)
        }
    }

    @EventHandler
    private fun onSignChange(event: SignChangeEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is Sign) {
            pylonBlock.onSignChange(event)
        }
    }

    @EventHandler
    private fun onVaultDisplayItem(event: VaultDisplayItemEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is TrialVault) {
            pylonBlock.onDisplayItem(event)
        }
    }

    @EventHandler
    private fun onLeafDecay(event: LeavesDecayEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if (pylonBlock is Leaf) {
            pylonBlock.onDecayNaturally(event)
        }
    }

    @EventHandler
    private fun onTargetHit(event: TargetHitEvent) {
        val pylonBlock = BlockStorage.get(event.hitBlock ?: return)
        if(pylonBlock is TargetBlock){
            pylonBlock.onHit(event)
        }
    }

    @EventHandler
    private fun onCompostByHopper(event: CompostItemEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if(pylonBlock is Composter){
            pylonBlock.onCompostByHopper(event)
        }
    }

    @EventHandler
    private fun onCompostByEntity(event: EntityCompostItemEvent) {
        val pylonBlock = BlockStorage.get(event.block)
        if(pylonBlock is Composter){
            pylonBlock.onCompostByEntity(event)
        }
    }
}