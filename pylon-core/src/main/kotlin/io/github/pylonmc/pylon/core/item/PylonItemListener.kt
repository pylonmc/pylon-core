package io.github.pylonmc.pylon.core.item

import com.destroystokyo.paper.event.player.PlayerReadyArrowEvent
import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.block.BlockStorage
import io.github.pylonmc.pylon.core.block.context.BlockItemContext
import io.github.pylonmc.pylon.core.item.base.*
import io.github.pylonmc.pylon.core.item.research.Research.Companion.canUse
import io.github.pylonmc.pylon.core.util.findPylonItemInInventory
import io.papermc.paper.event.player.PlayerPickItemEvent
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.inventory.BrewingStandFuelEvent
import org.bukkit.event.inventory.FurnaceBurnEvent
import org.bukkit.event.player.*

@Suppress("UnstableApiUsage")
internal object PylonItemListener : Listener {
    @EventHandler
    private fun handle(event: PlayerReadyArrowEvent) {
        val bow = PylonItem.fromStack(event.bow)
        if (bow != null && !event.player.canUse(bow, true)) {
            event.isCancelled = true
            return
        }
        if (bow is PylonBow) {
            try {
                bow.onBowReady(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, bow)
            }
        }

        val arrow = PylonItem.fromStack(event.arrow)
        if (arrow != null && !event.player.canUse(arrow, true)) {
            event.isCancelled = true
            return
        }
        if (arrow is PylonArrow) {
            try {
                arrow.onArrowReady(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, arrow)
            }
        }
    }

    @EventHandler
    private fun handle(event: EntityShootBowEvent) {
        val bow = event.bow?.let { PylonItem.fromStack(it) }
        if (bow is PylonBow) {
            try {
                bow.onBowFired(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, bow)
            }
        }

        val arrow = event.consumable?.let { PylonItem.fromStack(it) }
        if (arrow is PylonArrow) {
            try {
                arrow.onArrowShotFromBow(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, arrow)
            }
        }
    }

    @EventHandler
    private fun handle(event: PlayerInteractEvent) {
        val pylonItem = event.item?.let { PylonItem.fromStack(it) } ?: return
        if (!event.player.canUse(pylonItem, true)) {
            event.isCancelled = true
            return
        }
        if (
            pylonItem is PylonCooldownable &&
            event.player.getCooldown(pylonItem.stack) > 0 &&
            pylonItem.respectCooldown
        ) return
        if (pylonItem is PylonBlockInteractor && event.hasBlock()) {
            try {
                pylonItem.onUsedToClickBlock(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonItem)
            }
        }
        if (pylonItem is PylonInteractor) {
            try {
                pylonItem.onUsedToRightClick(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonItem)
            }
        }
    }

    @EventHandler
    private fun handle(event: BrewingStandFuelEvent) {
        val pylonItem = PylonItem.fromStack(event.fuel)
        if (pylonItem is PylonBrewingStandFuel) {
            try {
                pylonItem.onUsedAsBrewingStandFuel(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonItem)
            }
        }
    }

    @EventHandler
    private fun handle(event: PlayerBucketEmptyEvent) {
        val pylonItem = event.itemStack?.let { PylonItem.fromStack(it) }
        if (pylonItem != null && !event.player.canUse(pylonItem, true)) {
            event.isCancelled = true
            return
        }
        if (pylonItem is PylonBucket) {
            try {
                pylonItem.onBucketEmptied(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonItem)
            }
        }
    }

    @EventHandler
    private fun handle(event: PlayerBucketFillEvent) {
        val stack = event.player.inventory.getItem(event.hand)
        val pylonItem = PylonItem.fromStack(stack)
        if (pylonItem != null && !event.player.canUse(pylonItem, true)) {
            event.isCancelled = true
            return
        }
        if (pylonItem is PylonBucket) {
            try {
                pylonItem.onBucketFilled(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonItem)
            }
        }
    }

    @EventHandler
    private fun handle(event: PlayerItemConsumeEvent) {
        val pylonItem = PylonItem.fromStack(event.item)
        if (pylonItem != null && !event.player.canUse(pylonItem, true)) {
            event.isCancelled = true
            return
        }
        if (pylonItem is PylonConsumable) {
            try {
                pylonItem.onConsumed(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonItem)
            }
        }
    }

    @EventHandler
    private fun handle(event: PlayerItemDamageEvent) {
        val pylonItem = PylonItem.fromStack(event.item)
        if (pylonItem != null && !event.player.canUse(pylonItem, true)) {
            return
        }
        if (pylonItem is PylonItemDamageable) {
            try {
                pylonItem.onItemDamaged(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonItem)
            }
        }
    }

    @EventHandler
    private fun handle(event: PlayerItemBreakEvent) {
        val pylonItem = PylonItem.fromStack(event.brokenItem)
        if (pylonItem != null && !event.player.canUse(pylonItem, true)) {
            return
        }
        if (pylonItem is PylonItemDamageable) {
            try {
                pylonItem.onItemBreaks(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonItem)
            }
        }
    }

    @EventHandler
    private fun handle(event: PlayerItemMendEvent) {
        val pylonItem = PylonItem.fromStack(event.item)
        if (pylonItem != null && !event.player.canUse(pylonItem, true)) {
            event.isCancelled = true
            return
        }
        if (pylonItem is PylonItemDamageable && event.player.canUse(pylonItem, true)) {
            try {
                pylonItem.onItemMended(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonItem)
            }
        }
    }

    @EventHandler
    private fun handle(event: PlayerInteractEntityEvent) {
        val pylonItemMainHand = PylonItem.fromStack(event.player.inventory.itemInMainHand)
        if (pylonItemMainHand != null && !event.player.canUse(pylonItemMainHand, true)) {
            event.isCancelled = true
            return
        }
        if (pylonItemMainHand is PylonItemEntityInteractor &&
            !(event.player.getCooldown(pylonItemMainHand.stack) > 0 && pylonItemMainHand.respectCooldown)
        ) {
            try {
                pylonItemMainHand.onUsedToRightClickEntity(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonItemMainHand)
            }
        }

        val pylonItemOffHand = PylonItem.fromStack(event.player.inventory.itemInOffHand)
        if (pylonItemOffHand != null && !event.player.canUse(pylonItemOffHand, true)) {
            event.isCancelled = true
            return
        }
        if (pylonItemOffHand is PylonItemEntityInteractor &&
            !(event.player.getCooldown(pylonItemOffHand.stack) > 0 && pylonItemOffHand.respectCooldown)
        ) {
            try {
                pylonItemOffHand.onUsedToRightClickEntity(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonItemOffHand)
            }
        }
    }

    @EventHandler
    private fun handle(event: FurnaceBurnEvent) {
        val pylonItem = PylonItem.fromStack(event.fuel) ?: return
        if (pylonItem is VanillaCookingFuel) {
            try {
                pylonItem.onBurntAsFuel(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonItem)
            }
        }
    }

    @EventHandler
    private fun handle(event: BlockDamageEvent) {
        val pylonItem = PylonItem.fromStack(event.itemInHand)
        if (pylonItem != null && !event.player.canUse(pylonItem, true)) {
            event.isCancelled = true
            return
        }
        if (pylonItem is PylonTool) {
            try {
                pylonItem.onUsedToDamageBlock(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonItem)
            }
        }
    }

    @EventHandler
    private fun handle(event: BlockBreakEvent) {
        val pylonItemMainHand = PylonItem.fromStack(event.player.inventory.itemInMainHand)
        if (pylonItemMainHand != null && !event.player.canUse(pylonItemMainHand, true)) {
            event.isCancelled = true
            return
        }
        if (pylonItemMainHand is PylonTool) {
            try {
                pylonItemMainHand.onUsedToBreakBlock(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonItemMainHand)
            }
        }

        val pylonItemOffHand = PylonItem.fromStack(event.player.inventory.itemInOffHand)
        if (pylonItemOffHand != null && !event.player.canUse(pylonItemOffHand, true)) {
            event.isCancelled = true
            return
        }
        if (pylonItemOffHand is PylonTool) {
            try {
                pylonItemOffHand.onUsedToBreakBlock(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonItemOffHand)
            }
        }
    }

    @EventHandler
    private fun handle(event: EntityDamageByEntityEvent) {
        val damager = event.damageSource.causingEntity
        if (!event.damageSource.isIndirect) {
            if (damager is Player) {
                val pylonItemMainHand = PylonItem.fromStack(damager.inventory.itemInMainHand)
                if (pylonItemMainHand != null && !damager.canUse(pylonItemMainHand, true)) {
                    event.isCancelled = true
                    return
                }
                if (pylonItemMainHand is PylonWeapon) {
                    try {
                        pylonItemMainHand.onUsedToDamageEntity(event)
                    } catch (e: Exception) {
                        logEventHandleErr(event, e, pylonItemMainHand)
                    }
                }

                val pylonItemOffHand = PylonItem.fromStack(damager.inventory.itemInOffHand)
                if (pylonItemOffHand != null && !damager.canUse(pylonItemOffHand, true)) {
                    event.isCancelled = true
                    return
                }
                if (pylonItemOffHand is PylonWeapon) {
                    try {
                        pylonItemOffHand.onUsedToDamageEntity(event)
                    } catch (e: Exception) {
                        logEventHandleErr(event, e, pylonItemOffHand)
                    }
                }
            }
        }
    }

    @EventHandler
    private fun handle(event: EntityDeathEvent) {
        val killer = event.damageSource.causingEntity
        if (killer is Player) {
            val pylonItemMainHand = PylonItem.fromStack(killer.inventory.itemInMainHand)
            if (pylonItemMainHand != null && !killer.canUse(pylonItemMainHand, true)) {
                event.isCancelled = true
                return
            }
            if (pylonItemMainHand is PylonWeapon) {
                try {
                    pylonItemMainHand.onUsedToKillEntity(event)
                } catch (e: Exception) {
                    logEventHandleErr(event, e, pylonItemMainHand)
                }
            }

            val pylonItemOffHand = PylonItem.fromStack(killer.inventory.itemInOffHand)
            if (pylonItemOffHand != null && !killer.canUse(pylonItemOffHand, true)) {
                event.isCancelled = true
                return
            }
            if (pylonItemOffHand is PylonWeapon) {
                try {
                    pylonItemOffHand.onUsedToKillEntity(event)
                } catch (e: Exception) {
                    logEventHandleErr(event, e, pylonItemOffHand)
                }
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

    @JvmSynthetic
    internal fun logEventHandleErr(event: Event, e: Exception, item: PylonItem) {
        PylonCore.logger.severe("Error when handling item(${item.key}) event handler ${event.javaClass.simpleName}: ${e.localizedMessage}")
        e.printStackTrace()
    }
}