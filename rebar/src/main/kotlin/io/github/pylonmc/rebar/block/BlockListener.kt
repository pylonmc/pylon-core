package io.github.pylonmc.rebar.block

import com.destroystokyo.paper.event.block.BeaconEffectEvent
import com.destroystokyo.paper.event.block.BlockDestroyEvent
import com.destroystokyo.paper.event.player.PlayerJumpEvent
import io.github.pylonmc.rebar.Rebar
import io.github.pylonmc.rebar.block.base.*
import io.github.pylonmc.rebar.block.context.BlockBreakContext
import io.github.pylonmc.rebar.block.context.BlockCreateContext
import io.github.pylonmc.rebar.config.RebarConfig
import io.github.pylonmc.rebar.entity.EntityStorage
import io.github.pylonmc.rebar.event.RebarBlockUnloadEvent
import io.github.pylonmc.rebar.item.RebarItem
import io.github.pylonmc.rebar.item.research.Research.Companion.canUse
import io.github.pylonmc.rebar.util.damageItem
import io.github.pylonmc.rebar.util.isFakeEvent
import io.github.pylonmc.rebar.util.position.position
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.event.block.*
import io.papermc.paper.event.entity.EntityCompostItemEvent
import io.papermc.paper.event.player.*
import org.bukkit.ExplosionResult
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.block.Container
import org.bukkit.block.Hopper
import org.bukkit.entity.FallingBlock
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.block.BellRingEvent
import org.bukkit.event.enchantment.EnchantItemEvent
import org.bukkit.event.enchantment.PrepareItemEnchantEvent
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityDropItemEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.entity.EntityRemoveEvent
import org.bukkit.event.inventory.*
import org.bukkit.event.player.PlayerBucketEmptyEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerTakeLecternBookEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.event.world.StructureGrowEvent
import org.bukkit.inventory.BlockInventoryHolder
import org.bukkit.inventory.EquipmentSlot
import java.util.UUID
import java.util.WeakHashMap


/**
 * This listener listens for various events that would indicate a Rebar block either
 * being placed, removed, or moved
 *
 * It also handles components of multiblocks being placed, removed, or moved (this
 * includes vanilla blocks)
 */
@Suppress("UnstableApiUsage")
internal object BlockListener : Listener {
    private val blockErrMap: MutableMap<RebarBlock, Int> = WeakHashMap()

    @EventHandler(ignoreCancelled = true)
    private fun blockPlace(event: BlockPlaceEvent) {
        val item = event.itemInHand
        val player = event.player

        if (!item.type.isBlock) {
            return
        }

        val rebarItem = RebarItem.fromStack(item) ?: return
        if (!event.player.canUse(rebarItem, true)) {
            event.isCancelled = true
            return
        }

        if (isFakeEvent(event)) {
            // Fake events are for checking permissions, no need to do anything but check permission.
            if (rebarItem.schema.rebarBlockKey == null
                || BlockStorage.isRebarBlock(event.block)
            ) {
                event.isCancelled = true
                return
            }
        }

        val rebarBlock = rebarItem.place(BlockCreateContext.PlayerPlace(player, item, event))

        if (rebarBlock == null) {
            event.isCancelled = true
        }

        if (rebarBlock != null && player.gameMode != GameMode.CREATIVE) {
            player.inventory.getItem(event.hand).subtract()
        }
    }

    private val fallMap = HashMap<UUID, Pair<RebarFallingBlock, RebarFallingBlock.RebarFallingBlockEntity>>();

    @EventHandler(ignoreCancelled = true)
    private fun entityBlockChange(event: EntityChangeBlockEvent) {
        val entity = event.entity

        if (entity !is FallingBlock) return

        val block = event.block
        if (!entity.isInWorld) {
            val rebarBlock = BlockStorage.get(block) ?: return
            val rebarFallingBlock = rebarBlock as? RebarFallingBlock
            if (rebarFallingBlock == null) {
                event.isCancelled = true
                return
            }

            val blockPdc = RebarBlock.serialize(rebarBlock, block.chunk.persistentDataContainer.adapterContext)
            val fallingEntity = RebarFallingBlock.RebarFallingBlockEntity(rebarBlock.schema, blockPdc, block.position, entity)
            rebarFallingBlock.onFallStart(event, fallingEntity)
            if (!event.isCancelled) {
                BlockStorage.deleteBlock(block.position)
                EntityStorage.add(fallingEntity)
                // save this here as the entity storage is going to nuke it if the item drops
                fallMap[entity.uniqueId] = Pair(rebarFallingBlock, fallingEntity)
            }
        } else {
            val rebarEntity = EntityStorage.get(entity) as? RebarFallingBlock.RebarFallingBlockEntity ?: return
            val rebarBlock = BlockStorage.loadBlock(block.position, rebarEntity.blockSchema, rebarEntity.blockData) as RebarFallingBlock

            rebarBlock.onFallStop(event, rebarEntity)
        }
    }

    @EventHandler
    private fun entityDespawn(event: EntityRemoveEvent) {
        // DESPAWN = Fell and created block ; OUT_OF_WORLD = Fell and dropped item
        if (event.cause != EntityRemoveEvent.Cause.DESPAWN) return
        val entity = event.entity
        if (entity !is FallingBlock) return
        fallMap.remove(entity.uniqueId)
    }

    @EventHandler(ignoreCancelled = true)
    private fun fallingBlockDrop(event: EntityDropItemEvent) {
        val entity = event.entity

        if (entity !is FallingBlock) return

        val (rebarFallingBlock, rebarFallingEntity) = fallMap[entity.uniqueId] ?: return
        fallMap.remove(entity.uniqueId)

        val relativeItem = rebarFallingBlock.onItemDrop(event, rebarFallingEntity)
        if (event.isCancelled) return
        if (relativeItem == null) {
            event.isCancelled = true
            return
        }

        event.itemDrop.itemStack = relativeItem
    }

    @EventHandler(ignoreCancelled = true)
    private fun blockRemove(event: BlockBreakEvent) {
        if (BlockStorage.isRebarBlock(event.block)) {
            if (BlockStorage.breakBlock(event.block, BlockBreakContext.PlayerBreak(event)) == null) {
                event.isCancelled = true
                return
            }
            event.isDropItems = false
            event.expToDrop = 0

            val toolItem = event.player.equipment.getItem(EquipmentSlot.HAND)
            val tool = toolItem.getData(DataComponentTypes.TOOL) ?: return
            damageItem(toolItem, tool.damagePerBlock(), event.player, EquipmentSlot.HAND)
        }
    }

    @EventHandler(ignoreCancelled = true)
    private fun blockBurn(event: BlockBurnEvent) {
        if (BlockStorage.isRebarBlock(event.block)) {
            if (BlockStorage.breakBlock(event.block, BlockBreakContext.Burned(event)) == null) {
                event.isCancelled = true
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private fun blockRemove(event: BlockExplodeEvent) {
        if (event.explosionResult == ExplosionResult.TRIGGER_BLOCK || event.explosionResult == ExplosionResult.KEEP) {
            return
        }

        if (BlockStorage.isRebarBlock(event.block) && BlockStorage.breakBlock(event.block, BlockBreakContext.BlockExplosionOrigin(event)) == null) {
            event.isCancelled = true
            return
        }

        val it = event.blockList().iterator()
        while (it.hasNext()) {
            val block = it.next()
            if (BlockStorage.isRebarBlock(block) && BlockStorage.breakBlock(block, BlockBreakContext.BlockExploded(event)) == null) {
                it.remove()
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private fun blockRemove(event: EntityExplodeEvent) {
        if (event.explosionResult == ExplosionResult.TRIGGER_BLOCK || event.explosionResult == ExplosionResult.KEEP) {
            return
        }

        val it = event.blockList().iterator()
        while (it.hasNext()) {
            val block = it.next()
            if (BlockStorage.isRebarBlock(block) && BlockStorage.breakBlock(block, BlockBreakContext.EntityExploded(block, event)) == null) {
                it.remove()
            }
        }
    }

    // Event added by paper, not really documented when it's called so two separate handlers might
    // fire for some block breaks but this shouldn't be an issue
    // Primarily added to handle sensitive blocks
    @EventHandler(ignoreCancelled = true)
    private fun blockRemove(event: BlockDestroyEvent) {
        if (BlockStorage.isRebarBlock(event.block)) {
            if (BlockStorage.breakBlock(event.block, BlockBreakContext.Destroyed(event)) == null) {
                event.isCancelled = true
            }
            event.setWillDrop(false)
        }
    }

    @EventHandler(ignoreCancelled = true)
    private fun blockRemove(event: BlockFadeEvent) {
        if (BlockStorage.isRebarBlock(event.block)) {
            if (BlockStorage.breakBlock(event.block, BlockBreakContext.Faded(event)) == null) {
                event.isCancelled = true
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private fun disallowForming(event: BlockFormEvent) {
        if (BlockStorage.isRebarBlock(event.block)) {
            event.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    private fun disallowFromTo(event: BlockFromToEvent) {
        if (BlockStorage.isRebarBlock(event.toBlock)) {
            event.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    private fun disallowMovementByPistons(event: BlockPistonExtendEvent) {
        for (block in event.blocks) {
            if (BlockStorage.isRebarBlock(block)) {
                event.isCancelled = true
                return
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private fun disallowMovementByPistons(event: BlockPistonRetractEvent) {
        for (block in event.blocks) {
            if (BlockStorage.isRebarBlock(block)) {
                event.isCancelled = true
                return
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private fun disallowStructureGrow(event: StructureGrowEvent) {
        for (state in event.blocks) {
            if (BlockStorage.isRebarBlock(state.block)) {
                event.isCancelled = true
                return
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    private fun preventReplacingStructureVoids(event: BlockPlaceEvent) {
        val rebarBlock = BlockStorage.get(event.block)
        if (rebarBlock != null && rebarBlock.schema.material == Material.STRUCTURE_VOID) {
            event.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    private fun onFluidPlace(event: PlayerBucketEmptyEvent) {
        val rebarBlock = BlockStorage.get(event.block)
        if (rebarBlock != null && rebarBlock.schema.material == Material.STRUCTURE_VOID) {
            event.isCancelled = true
        }
    }

    @EventHandler
    private fun onBeaconActivate(event: BeaconActivatedEvent) {
        val rebarBlock = BlockStorage.get(event.block)
        if (rebarBlock is RebarBeacon) {
            try {
                rebarBlock.onActivated(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarBlock)
            }
        }
    }

    @EventHandler
    private fun onBeaconDeactivate(event: BeaconDeactivatedEvent) {
        val rebarBlock = BlockStorage.get(event.block)
        if (rebarBlock is RebarBeacon) {
            try {
                rebarBlock.onDeactivated(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarBlock)
            }
        }
    }

    @EventHandler
    private fun onBeaconChangeEffect(event: PlayerChangeBeaconEffectEvent) {
        val rebarBlock = BlockStorage.get(event.beacon)
        if (rebarBlock is RebarBeacon) {
            try {
                rebarBlock.onEffectChange(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarBlock)
            }
        }
    }

    @EventHandler
    private fun onBeaconEffectApply(event: BeaconEffectEvent) {
        val rebarBlock = BlockStorage.get(event.block)
        if (rebarBlock is RebarBeacon) {
            try {
                rebarBlock.onEffectApply(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarBlock)
            }
        }
    }

    @EventHandler
    private fun onBellRing(event: BellRingEvent) {
        val rebarBlock= BlockStorage.get(event.block)
        if (rebarBlock is RebarBell) {
            try {
                rebarBlock.onRing(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarBlock)
            }
        }
    }

    @EventHandler
    private fun onBellResonate(event: BellResonateEvent) {
        val rebarBlock = BlockStorage.get(event.block)
        if (rebarBlock is RebarBell) {
            try {
                rebarBlock.onResonate(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarBlock)
            }
        }
    }

    @EventHandler
    private fun onTNTIgnite(event: TNTPrimeEvent) {
        val rebarBlock = BlockStorage.get(event.block)
        if (rebarBlock is RebarTNT) {
            try {
                rebarBlock.onIgnite(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarBlock)
            }
        }
    }

    @EventHandler
    private fun onNotePlay(event: NotePlayEvent) {
        val rebarBlock = BlockStorage.get(event.block)
        if (rebarBlock is RebarNoteBlock) {
            try {
                rebarBlock.onNotePlay(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarBlock)
            }
        }
    }

    @EventHandler
    private fun onCrafterCraft(event: CrafterCraftEvent) {
        val rebarBlock = BlockStorage.get(event.block)
        if (rebarBlock is RebarCrafter) {
            try {
                rebarBlock.onCraft(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarBlock)
            }
        }
    }

    @EventHandler
    private fun onSpongeAbsorb(event: SpongeAbsorbEvent) {
        val rebarBlock = BlockStorage.get(event.block)
        if (rebarBlock is RebarSponge) {
            try {
                rebarBlock.onAbsorb(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarBlock)
            }
        }
    }

    @EventHandler
    private fun onStartCook(event: InventoryBlockStartEvent) {
        val rebarBlock = BlockStorage.get(event.block)
        if (rebarBlock is RebarFurnace) {
            try {
                rebarBlock.onStartSmelting(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarBlock)
            }
        } else if (rebarBlock is RebarCampfire) {
            try {
                rebarBlock.onStartCooking(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarBlock)
            }
        } else if (rebarBlock is RebarBrewingStand) {
            try {
                rebarBlock.onStartBrewing(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarBlock)
            }
        }
    }

    @EventHandler
    private fun onFinishCook(event: BlockCookEvent) {
        val rebarBlock = BlockStorage.get(event.block)
        if (rebarBlock is RebarFurnace) {
            try {
                rebarBlock.onEndSmelting(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarBlock)
            }
        } else if (rebarBlock is RebarCampfire) {
            try {
                rebarBlock.onEndCooking(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarBlock)
            }
        } else if (rebarBlock is RebarBrewingStand) {
            try {
                rebarBlock.onEndBrewing(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarBlock)
            }
        }
    }

    @EventHandler
    private fun onDispenseArmor(event: BlockDispenseArmorEvent) {
        val rebarBlock = BlockStorage.get(event.block)
        if (rebarBlock is RebarDispenser) {
            try {
                rebarBlock.onDispenseArmor(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarBlock)
            }
        }
    }

    @EventHandler
    private fun onDispenseItem(event: BlockDispenseEvent) {
        val rebarBlock = BlockStorage.get(event.block)
        if (rebarBlock is RebarDispenser) {
            try {
                rebarBlock.onDispenseItem(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarBlock)
            }
        }
    }

    @EventHandler
    private fun onDispenseLoot(event: BlockDispenseLootEvent) {
        val rebarBlock = BlockStorage.get(event.block)
        if (rebarBlock is RebarDispenser) {
            try {
                rebarBlock.onDispenseLoot(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarBlock)
            }
        }
    }

    @EventHandler
    private fun onDispenserShearSheep(event: BlockShearEntityEvent) {
        val rebarBlock = BlockStorage.get(event.block)
        if (rebarBlock is RebarDispenser) {
            try {
                rebarBlock.onShearSheep(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarBlock)
            }
        }
    }

    @EventHandler
    private fun onBlockGrow(event: BlockGrowEvent) {
        val rebarBlock = BlockStorage.get(event.block)
        if (rebarBlock is RebarGrowable) {
            try {
                rebarBlock.onGrow(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarBlock)
            }
        }
    }

    @EventHandler
    private fun onBlockFertilize(event: BlockFertilizeEvent) {
        val rebarBlock = BlockStorage.get(event.block)
        if (rebarBlock is RebarGrowable) {
            try {
                rebarBlock.onFertilize(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarBlock)
            }
        }
    }

    @EventHandler
    private fun onCauldronLevelChange(event: CauldronLevelChangeEvent) {
        val rebarBlock = BlockStorage.get(event.block)
        if (rebarBlock is RebarCauldron) {
            try {
                rebarBlock.onLevelChange(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarBlock)
            }
        }
    }

    @EventHandler
    private fun onSignChange(event: SignChangeEvent) {
        val rebarBlock = BlockStorage.get(event.block)
        if (rebarBlock is RebarSign) {
            try {
                rebarBlock.onSignChange(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarBlock)
            }
        }
    }

    @EventHandler
    private fun onVaultDisplayItem(event: VaultDisplayItemEvent) {
        val rebarBlock = BlockStorage.get(event.block)
        if (rebarBlock is RebarTrialVault) {
            try {
                rebarBlock.onDisplayItem(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarBlock)
            }
        }
    }

    @EventHandler
    private fun onLeafDecay(event: LeavesDecayEvent) {
        val rebarBlock = BlockStorage.get(event.block)
        if (rebarBlock is RebarLeaf) {
            try {
                rebarBlock.onDecayNaturally(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarBlock)
            }
        }
    }

    @EventHandler
    private fun onTargetHit(event: TargetHitEvent) {
        val rebarBlock = BlockStorage.get(event.hitBlock ?: return)
        if (rebarBlock is RebarTargetBlock) {
            try {
                rebarBlock.onHit(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarBlock)
            }
        }
    }

    @EventHandler
    private fun onCompostByHopper(event: CompostItemEvent) {
        val rebarBlock = BlockStorage.get(event.block)
        if (rebarBlock is RebarComposter) {
            try {
                rebarBlock.onCompostByHopper(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarBlock)
            }
        }
    }

    @EventHandler
    private fun onCompostByEntity(event: EntityCompostItemEvent) {
        val rebarBlock = BlockStorage.get(event.block)
        if (rebarBlock is RebarComposter) {
            try {
                rebarBlock.onCompostByEntity(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarBlock)
            }
        }
    }

    @EventHandler
    private fun onShearBlock(event: PlayerShearBlockEvent) {
        val rebarBlock = BlockStorage.get(event.block)
        if (rebarBlock is RebarShearable) {
            try {
                rebarBlock.onShear(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarBlock)
            }
        }
    }

    @EventHandler
    private fun onLecternInsertBook(event: PlayerInsertLecternBookEvent) {
        val rebarBlock = BlockStorage.get(event.block)
        if (rebarBlock is RebarLectern) {
            try {
                rebarBlock.onInsertBook(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarBlock)
            }
        }
    }

    @EventHandler
    private fun onLecternRemoveBook(event: PlayerTakeLecternBookEvent) {
        val rebarBlock = BlockStorage.get(event.lectern.block)
        if (rebarBlock is RebarLectern) {
            try {
                rebarBlock.onRemoveBook(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarBlock)
            }
        }
    }

    @EventHandler
    private fun onLecternChangePage(event: PlayerLecternPageChangeEvent) {
        val rebarBlock = BlockStorage.get(event.lectern.block)
        if (rebarBlock is RebarLectern) {
            try {
                rebarBlock.onChangePage(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarBlock)
            }
        }
    }

    @EventHandler
    private fun onPistonExtend(event: BlockPistonExtendEvent) {
        val rebarBlock = BlockStorage.get(event.block)
        if (rebarBlock is RebarPiston) {
            try {
                rebarBlock.onExtend(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarBlock)
            }
        }
    }

    @EventHandler
    private fun onPistonRetract(event: BlockPistonRetractEvent) {
        val rebarBlock = BlockStorage.get(event.block)
        if (rebarBlock is RebarPiston) {
            try {
                rebarBlock.onRetract(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarBlock)
            }
        }
    }

    @EventHandler
    private fun onPreEnchant(event: PrepareItemEnchantEvent) {
        val rebarBlock = BlockStorage.get(event.enchantBlock)
        if (rebarBlock is RebarEnchantingTable) {
            try {
                rebarBlock.onPrepareEnchant(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarBlock)
            }
        }
    }

    @EventHandler
    private fun onEnchant(event: EnchantItemEvent) {
        val rebarBlock = BlockStorage.get(event.enchantBlock)
        if (rebarBlock is RebarEnchantingTable) {
            try {
                rebarBlock.onEnchant(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarBlock)
            }
        }
    }

    @EventHandler
    private fun onRedstoneCurrentChange(event: BlockRedstoneEvent) {
        val rebarBlock = BlockStorage.get(event.block)
        if (rebarBlock is RebarRedstoneBlock) {
            try {
                rebarBlock.onCurrentChange(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarBlock)
            }
        }
    }

    @EventHandler
    private fun onBrewingStandFuel(event: BrewingStandFuelEvent) {
        val rebarBlock = BlockStorage.get(event.block)
        if (rebarBlock is RebarBrewingStand) {
            try {
                rebarBlock.onFuel(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarBlock)
            }
        }
    }

    @EventHandler
    private fun onPreDispense(event: BlockPreDispenseEvent) {
        val rebarBlock = BlockStorage.get(event.block)
        if (rebarBlock is RebarDispenser) {
            try {
                rebarBlock.onPreDispense(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarBlock)
            }
        }
    }

    @EventHandler
    private fun onFailDispense(event: BlockFailedDispenseEvent) {
        val rebarBlock = BlockStorage.get(event.block)
        if (rebarBlock is RebarDispenser) {
            try {
                rebarBlock.onFailDispense(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarBlock)
            }
        }
    }

    @EventHandler
    private fun onFurnaceExtract(event: FurnaceExtractEvent) {
        val rebarBlock = BlockStorage.get(event.block)
        if (rebarBlock is RebarFurnace) {
            try {
                rebarBlock.onExtractItem(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarBlock)
            }
        }
    }

    @EventHandler
    private fun onFurnaceBurnFuel(event: FurnaceBurnEvent) {
        val rebarBlock = BlockStorage.get(event.block)
        if (rebarBlock is RebarFurnace) {
            try {
                rebarBlock.onFuelBurn(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarBlock)
            }
        }
    }

    @EventHandler
    private fun onSignOpen(event: PlayerOpenSignEvent) {
        val rebarBlock = BlockStorage.get(event.sign.block)
        if (rebarBlock is RebarSign) {
            try {
                rebarBlock.onOpen(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarBlock)
            }
        }
    }

    @EventHandler
    private fun onPlayerBlockInteract(event: PlayerInteractEvent) {
        val rebarBlock = BlockStorage.get(event.clickedBlock ?: return)
        if (rebarBlock is RebarInteractBlock) {
            try {
                rebarBlock.onInteract(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarBlock)
            }
        }
    }

    @EventHandler
    private fun onPlayerToggleSneak(event: PlayerToggleSneakEvent) {
        val blockUnder = event.player.location.add(0.0, -1.0, 0.0).block
        val blockIn = event.player.location.add(0.0, 0.0, 0.0).block
        val rebarBlock = BlockStorage.get(blockUnder) ?: BlockStorage.get(blockIn)
        if (rebarBlock is RebarSneakableBlock) {
            /*
            * Event player is from before the event is triggered, so when the player
            * is marked as *not* sneaking, they just toggled it.
            */
            if (!event.player.isSneaking) {
                try {
                    rebarBlock.onSneakedOn(event)
                } catch (e: Exception) {
                    logEventHandleErr(event, e, rebarBlock)
                }
            } else {
                try {
                    rebarBlock.onUnsneakedOn(event)
                } catch (e: Exception) {
                    logEventHandleErr(event, e, rebarBlock)
                }
            }
        }
    }

    @EventHandler
    private fun onPlayerJumpEvent(event: PlayerJumpEvent) {
        val blockUnder = event.player.location.add(0.0, -1.0, 0.0).block
        val blockIn = event.player.location.add(0.0, 0.0, 0.0).block
        val rebarBlock = BlockStorage.get(blockUnder) ?: BlockStorage.get(blockIn)
        if (rebarBlock is RebarJumpBlock) {
            try {
                rebarBlock.onJumpedOn(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarBlock)
            }
        }
    }

    @EventHandler
    private fun onUnload(event: RebarBlockUnloadEvent) {
        if (event.rebarBlock is RebarUnloadBlock) {
            try {
                event.rebarBlock.onUnload(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, event.rebarBlock)
            }
        }
    }

    @EventHandler
    private fun onFlowerpotManipulate(event: PlayerFlowerPotManipulateEvent) {
        val block = BlockStorage.get(event.flowerpot)
        if (block is RebarFlowerPot) {
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
            if (block is RebarVanillaContainerBlock) {
                block.onInventoryOpen(event)
            }
        }
    }

    @EventHandler
    private fun onItemMove(event: InventoryMoveItemEvent) {
        val sourceHolder = event.source.holder
        if (sourceHolder is BlockInventoryHolder) {
            val sourceBlock = BlockStorage.get(sourceHolder.block)
            if (sourceBlock is RebarVanillaContainerBlock) {
                sourceBlock.onItemMoveFrom(event)
            }
        }
        val destHolder = event.destination.holder
        if (destHolder is BlockInventoryHolder) {
            val destBlock = BlockStorage.get(destHolder.block)
            if (destBlock is RebarVanillaContainerBlock) {
                destBlock.onItemMoveTo(event)
            }
        }
    }

    @EventHandler
    private fun onInventoryPickup(event: InventoryPickupItemEvent) {
        val inv = event.inventory
        val holder = inv.holder
        if (holder is Hopper) {
            val pyBlock = BlockStorage.get(holder.block) as? RebarHopper ?: return
            pyBlock.onHopperPickUpItem(event)
        }
    }



    @JvmSynthetic
    internal fun logEventHandleErr(event: Event?, e: Exception, block: RebarBlock) {
        if (event != null) {
            Rebar.logger.severe("Error when handling block(${block.key}, ${block.block.location}) event handler ${event.javaClass.simpleName}: ${e.localizedMessage}")
        } else {
            Rebar.logger.severe("Error when handling block(${block.key}, ${block.block.location}) ticking: ${e.localizedMessage}")
        }
        e.printStackTrace()
        blockErrMap[block] = blockErrMap[block]?.plus(1) ?: 1
        if (blockErrMap[block]!! > RebarConfig.ALLOWED_BLOCK_ERRORS) {
            BlockStorage.makePhantom(block)
            if (block is RebarTickingBlock) {
                RebarTickingBlock.stopTicking(block)
            }
        }
    }
}