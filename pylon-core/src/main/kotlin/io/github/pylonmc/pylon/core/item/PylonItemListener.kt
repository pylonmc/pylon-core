package io.github.pylonmc.pylon.core.item

import com.destroystokyo.paper.event.player.PlayerReadyArrowEvent
import io.github.pylonmc.pylon.core.block.context.BlockCreateContext
import io.github.pylonmc.pylon.core.block.context.BlockItemContext
import io.github.pylonmc.pylon.core.item.base.*
import io.github.pylonmc.pylon.core.persistence.blockstorage.BlockStorage
import io.github.pylonmc.pylon.core.util.findPylonItemInInventory
import io.papermc.paper.event.player.PlayerPickItemEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityInteractEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.inventory.BrewingStandFuelEvent
import org.bukkit.event.inventory.FurnaceBurnEvent
import org.bukkit.event.player.*

@Suppress("UnstableApiUsage")
internal object PylonItemListener : Listener {
    @EventHandler
    private fun handle(event: PlayerReadyArrowEvent) {
        val bow = PylonItem.fromStack(event.bow)
        if (bow is Bow) {
            bow.onBowReady(event)
        }

        val arrow = PylonItem.fromStack(event.arrow)
        if (arrow is Arrow) {
            arrow.onArrowReady(event)
        }
    }

    @EventHandler
    private fun handle(event: EntityShootBowEvent) {
        val bow = event.bow?.let { PylonItem.fromStack(it) }
        if (bow is Bow) {
            bow.onBowFired(event)
        }

        val arrow = event.consumable?.let { PylonItem.fromStack(it) }
        if (arrow is Arrow) {
            arrow.onArrowShotFromBow(event)
        }
    }

    @EventHandler
    private fun handle(event: PlayerInteractEvent) {
        val pylonItem = event.item?.let { PylonItem.fromStack(it) }
        if (pylonItem is BlockPlacer && event.action == Action.RIGHT_CLICK_BLOCK) {
            val context = BlockCreateContext.PlayerPlace(event.player, event.item!!)
            pylonItem.doPlace(context, event.clickedBlock!!.getRelative(event.blockFace))
        }
        if (pylonItem is BlockInteractor && event.hasBlock()) {
            pylonItem.onUsedToClickBlock(event)
        }
        if (pylonItem is Interactor) {
            pylonItem.onUsedToRightClick(event)
        }
    }

    @EventHandler
    private fun handle(event: BrewingStandFuelEvent) {
        val pylonItem = PylonItem.fromStack(event.fuel)
        if (pylonItem is BrewingStandFuel) {
            pylonItem.onUsedAsBrewingStandFuel(event)
        }
    }

    @EventHandler
    private fun handle(event: PlayerBucketEmptyEvent) {
        val pylonItem = event.itemStack?.let { PylonItem.fromStack(it) }
        if (pylonItem is Bucket) {
            pylonItem.onBucketEmptied(event)
        }
    }

    @EventHandler
    private fun handle(event: PlayerBucketFillEvent) {
        val pylonItem = event.itemStack?.let { PylonItem.fromStack(it) }
        if (pylonItem is Bucket) {
            pylonItem.onBucketFilled(event)
        }
    }

    @EventHandler
    private fun handle(event: PlayerItemConsumeEvent) {
        val pylonItem = PylonItem.fromStack(event.item)
        if (pylonItem is Consumable) {
            pylonItem.onConsumed(event)
        }
    }

    @EventHandler
    private fun handle(event: PlayerItemDamageEvent) {
        val pylonItem = PylonItem.fromStack(event.item)
        if (pylonItem is Damageable) {
            pylonItem.onItemDamaged(event)
        }
    }

    @EventHandler
    private fun handle(event: PlayerItemBreakEvent) {
        val pylonItem = PylonItem.fromStack(event.brokenItem)
        if (pylonItem is Damageable) {
            pylonItem.onItemBreaks(event)
        }
    }

    @EventHandler
    private fun handle(event: PlayerItemMendEvent) {
        val pylonItem = PylonItem.fromStack(event.item)
        if (pylonItem is Damageable) {
            pylonItem.onItemMended(event)
        }
    }

    @EventHandler
    private fun handle(event: EntityInteractEvent) {
        val entity = event.entity
        if (entity is Player) {
            val pylonItem = PylonItem.fromStack(entity.activeItem)
            if (pylonItem is EntityInteractor) {
                pylonItem.onUsedToRightClickEntity(event)
            }
        }
    }

    @EventHandler
    private fun handle(event: FurnaceBurnEvent) {
        val pylonItem = PylonItem.fromStack(event.fuel) ?: return
        if (pylonItem is VanillaCookingFuel) {
            pylonItem.onBurntAsFuel(event)
        } else {
            event.isCancelled = true
        }
    }

    @EventHandler
    private fun handle(event: BlockDamageEvent) {
        val pylonItem = PylonItem.fromStack(event.itemInHand)
        if (pylonItem is Tool) {
            pylonItem.onUsedToDamageBlock(event)
        }
    }

    @EventHandler
    private fun handle(event: BlockBreakEvent) {
        val pylonItem = PylonItem.fromStack(event.player.inventory.itemInMainHand)
        if (pylonItem is Tool) {
            pylonItem.onUsedToBreakBlock(event)
        }
    }

    @EventHandler
    private fun handle(event: EntityDamageByEntityEvent) {
        val damager = event.damageSource.causingEntity
        if (!event.damageSource.isIndirect) {
            if (damager is Player) {
                val pylonItem = PylonItem.fromStack(damager.inventory.itemInMainHand)
                if (pylonItem is Weapon) {
                    pylonItem.onUsedToDamageEntity(event)
                }
            }
        }
    }

    @EventHandler
    private fun handle(event: EntityDeathEvent) {
        val killer = event.damageSource.causingEntity
        if (killer is Player) {
            val pylonItem = PylonItem.fromStack(killer.inventory.itemInMainHand)
            if (pylonItem is Weapon) {
                pylonItem.onUsedToKillEntity(event)
            }
        }
    }

    @EventHandler
    private fun handle(event: PlayerPickItemEvent) {
        val block = event.player.getTargetBlockExact(4) ?: return
        val pylonBlock = BlockStorage.get(block) ?: return
        val blockItem = pylonBlock.getItem(BlockItemContext.PickBlock(event.player)) ?: return
        val blockPylonItem = PylonItem.fromStack(blockItem) ?: return

        val sourceSlot = event.sourceSlot
        if (sourceSlot != -1) {
            val sourceItem = event.player.inventory.getItem(event.sourceSlot)
            if (sourceItem != null) {
                val sourcePylonItem = PylonItem.fromStack(sourceItem)
                if (sourcePylonItem != null) {
                    // The source item is already of the correct Pylon type, so we shouldn't interfere with the event
                    return
                }
            }
        }

        // If we reach this point, the source item is not of the correct type
        // So we're going to search the inventory for a block of the correct type
        val existingSlot = findPylonItemInInventory(event.player.inventory, blockPylonItem)
        if (existingSlot != null) {
            // If we find one, we'll set the source to that slot
            event.sourceSlot = existingSlot
            // And if the item is in the hotbar, that should become the target (0-8 are hotbar slots
            if (existingSlot <= 8) {
                event.targetSlot = existingSlot
            }
            return
        }

        // Otherwise, we'll just attempt to add a new item and set the source to be that item
        if (event.player.inventory.addItem(blockItem).isNotEmpty()) {
            // Inventory full, can't pick the item
            event.isCancelled = true
            return
        }

        val newSourceSlot = findPylonItemInInventory(event.player.inventory, blockPylonItem)
        if (newSourceSlot == null) {
            // should never happen but you never know
            event.isCancelled = true
            return
        }

        event.sourceSlot = newSourceSlot
        event.targetSlot = event.player.inventory.heldItemSlot

        // don't question this idk wtf is going on - seems we have to manually do the swap in the hotbar
        if (sourceSlot <= 8) {
            val source = event.player.inventory.getItem(event.sourceSlot)
            val target = event.player.inventory.getItem(event.targetSlot)
            event.player.inventory.setItem(event.sourceSlot, target)
            event.player.inventory.setItem(event.targetSlot, source)
        }
    }
}