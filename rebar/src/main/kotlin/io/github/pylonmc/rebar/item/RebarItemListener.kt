package io.github.pylonmc.rebar.item

import com.destroystokyo.paper.event.player.PlayerReadyArrowEvent
import io.github.pylonmc.rebar.Rebar
import io.github.pylonmc.rebar.block.BlockStorage
import io.github.pylonmc.rebar.item.base.*
import io.github.pylonmc.rebar.item.research.Research.Companion.canUse
import io.github.pylonmc.rebar.util.findRebarItemInInventory
import io.papermc.paper.event.player.PlayerPickItemEvent
import org.bukkit.GameMode
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.event.block.BlockDispenseEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.inventory.BrewingStandFuelEvent
import org.bukkit.event.inventory.FurnaceBurnEvent
import org.bukkit.event.player.*
import kotlin.math.ceil

@Suppress("UnstableApiUsage")
internal object RebarItemListener : Listener {
    @EventHandler
    private fun handle(event: PlayerReadyArrowEvent) {
        val bow = RebarItem.fromStack(event.bow)
        if (bow != null && !event.player.canUse(bow, true)) {
            event.isCancelled = true
            return
        }
        if (bow is RebarBow) {
            try {
                bow.onBowReady(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, bow)
            }
        }

        val arrow = RebarItem.fromStack(event.arrow)
        if (arrow != null && !event.player.canUse(arrow, true)) {
            event.isCancelled = true
            return
        }
        if (arrow is RebarArrow) {
            try {
                arrow.onArrowReady(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, arrow)
            }
        }
    }

    @EventHandler
    private fun handle(event: EntityShootBowEvent) {
        val bow = event.bow?.let { RebarItem.fromStack(it) }
        if (bow is RebarBow) {
            try {
                bow.onBowFired(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, bow)
            }
        }

        val arrow = event.consumable?.let { RebarItem.fromStack(it) }
        if (arrow is RebarArrow) {
            try {
                arrow.onArrowShotFromBow(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, arrow)
            }
        }
    }

    @EventHandler
    private fun handle(event: PlayerInteractEvent) {
        val rebarItem = event.item?.let { RebarItem.fromStack(it) } ?: return
        if (!event.player.canUse(rebarItem, true)) {
            event.isCancelled = true
            return
        }
        if (
            rebarItem is RebarCooldownable &&
            event.player.getCooldown(rebarItem.stack) > 0 &&
            rebarItem.respectCooldown
        ) {
            event.isCancelled = true
            return
        }
        if (rebarItem is RebarBlockInteractor && event.hasBlock()) {
            try {
                rebarItem.onUsedToClickBlock(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarItem)
            }
        }
        if (rebarItem is RebarInteractor) {
            try {
                rebarItem.onUsedToRightClick(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarItem)
            }
        }
    }

    @EventHandler
    private fun handle(event: BrewingStandFuelEvent) {
        val rebarItem = RebarItem.fromStack(event.fuel)
        if (rebarItem is RebarBrewingStandFuel) {
            try {
                rebarItem.onUsedAsBrewingStandFuel(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarItem)
            }
        }
    }

    @EventHandler
    private fun handle(event: PlayerBucketEmptyEvent) {
        val rebarItem = event.itemStack?.let { RebarItem.fromStack(it) }
        if (rebarItem != null && !event.player.canUse(rebarItem, true)) {
            event.isCancelled = true
            return
        }
        if (rebarItem is RebarBucket) {
            try {
                rebarItem.onBucketEmptied(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarItem)
            }
        }
    }

    @EventHandler
    private fun handle(event: BlockDispenseEvent) {
        val rebarItem = RebarItem.fromStack(event.item)
        val dispensable = rebarItem as? RebarDispensable ?: return

        try {
            dispensable.onDispense(event)
        } catch (e: Exception) {
            logEventHandleErr(event, e, rebarItem)
        }
    }

    @EventHandler
    private fun handle(event: PlayerBucketFillEvent) {
        val stack = event.player.inventory.getItem(event.hand)
        val rebarItem = RebarItem.fromStack(stack)
        if (rebarItem != null && !event.player.canUse(rebarItem, true)) {
            event.isCancelled = true
            return
        }
        if (rebarItem is RebarBucket) {
            try {
                rebarItem.onBucketFilled(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarItem)
            }
        }
    }

    @EventHandler
    private fun handle(event: PlayerItemConsumeEvent) {
        val rebarItem = RebarItem.fromStack(event.item)
        if (rebarItem != null && !event.player.canUse(rebarItem, true)) {
            event.isCancelled = true
            return
        }
        if (rebarItem is RebarConsumable) {
            try {
                rebarItem.onConsumed(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarItem)
            }
        }
    }

    @EventHandler
    private fun handle(event: PlayerItemDamageEvent) {
        val rebarItem = RebarItem.fromStack(event.item)
        if (rebarItem != null && !event.player.canUse(rebarItem, true)) {
            return
        }
        if (rebarItem is RebarItemDamageable) {
            try {
                rebarItem.onItemDamaged(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarItem)
            }
        }
    }

    @EventHandler
    private fun handle(event: PlayerItemBreakEvent) {
        val rebarItem = RebarItem.fromStack(event.brokenItem)
        if (rebarItem != null && !event.player.canUse(rebarItem, true)) {
            return
        }
        if (rebarItem is RebarItemDamageable) {
            try {
                rebarItem.onItemBreaks(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarItem)
            }
        }
    }

    @EventHandler
    private fun handle(event: PlayerItemMendEvent) {
        val rebarItem = RebarItem.fromStack(event.item)
        if (rebarItem != null && !event.player.canUse(rebarItem, true)) {
            event.isCancelled = true
            return
        }
        if (rebarItem is RebarItemDamageable && event.player.canUse(rebarItem, true)) {
            try {
                rebarItem.onItemMended(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarItem)
            }
        }
    }

    @EventHandler
    private fun handle(event: PlayerInteractEntityEvent) {
        val rebarItemMainHand = RebarItem.fromStack(event.player.inventory.itemInMainHand)
        if (rebarItemMainHand != null && !event.player.canUse(rebarItemMainHand, true)) {
            event.isCancelled = true
            return
        }
        if (rebarItemMainHand is RebarItemEntityInteractor &&
            !(event.player.getCooldown(rebarItemMainHand.stack) > 0 && rebarItemMainHand.respectCooldown)
        ) {
            try {
                rebarItemMainHand.onUsedToRightClickEntity(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarItemMainHand)
            }
        }

        val rebarItemOffHand = RebarItem.fromStack(event.player.inventory.itemInOffHand)
        if (rebarItemOffHand != null && !event.player.canUse(rebarItemOffHand, true)) {
            event.isCancelled = true
            return
        }
        if (rebarItemOffHand is RebarItemEntityInteractor &&
            !(event.player.getCooldown(rebarItemOffHand.stack) > 0 && rebarItemOffHand.respectCooldown)
        ) {
            try {
                rebarItemOffHand.onUsedToRightClickEntity(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarItemOffHand)
            }
        }
    }

    @EventHandler
    private fun handle(event: FurnaceBurnEvent) {
        val rebarItem = RebarItem.fromStack(event.fuel) ?: return
        if (rebarItem is VanillaCookingFuel) {
            try {
                rebarItem.onBurntAsFuel(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarItem)
            }
        }
    }

    @EventHandler
    private fun handle(event: BlockDamageEvent) {
        val rebarItem = RebarItem.fromStack(event.itemInHand)
        if (rebarItem != null && !event.player.canUse(rebarItem, true)) {
            event.isCancelled = true
            return
        }
        if (rebarItem is RebarTool) {
            try {
                rebarItem.onUsedToDamageBlock(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarItem)
            }
        }
    }

    @EventHandler
    private fun handle(event: BlockBreakEvent) {
        val rebarItemMainHand = RebarItem.fromStack(event.player.inventory.itemInMainHand)
        if (rebarItemMainHand != null && !event.player.canUse(rebarItemMainHand, true)) {
            event.isCancelled = true
            return
        }
        if (rebarItemMainHand is RebarTool) {
            try {
                rebarItemMainHand.onUsedToBreakBlock(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarItemMainHand)
            }
        }

        val rebarItemOffHand = RebarItem.fromStack(event.player.inventory.itemInOffHand)
        if (rebarItemOffHand != null && !event.player.canUse(rebarItemOffHand, true)) {
            event.isCancelled = true
            return
        }
        if (rebarItemOffHand is RebarTool) {
            try {
                rebarItemOffHand.onUsedToBreakBlock(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarItemOffHand)
            }
        }
    }

    @EventHandler
    private fun handle(event: EntityDamageByEntityEvent) {
        val damager = event.damageSource.causingEntity
        if (!event.damageSource.isIndirect) {
            if (damager is Player) {
                val rebarItemMainHand = RebarItem.fromStack(damager.inventory.itemInMainHand)
                if (rebarItemMainHand != null && !damager.canUse(rebarItemMainHand, true)) {
                    event.isCancelled = true
                    return
                }
                if (rebarItemMainHand is RebarWeapon) {
                    try {
                        rebarItemMainHand.onUsedToDamageEntity(event)
                    } catch (e: Exception) {
                        logEventHandleErr(event, e, rebarItemMainHand)
                    }
                }

                val rebarItemOffHand = RebarItem.fromStack(damager.inventory.itemInOffHand)
                if (rebarItemOffHand != null && !damager.canUse(rebarItemOffHand, true)) {
                    event.isCancelled = true
                    return
                }
                if (rebarItemOffHand is RebarWeapon) {
                    try {
                        rebarItemOffHand.onUsedToDamageEntity(event)
                    } catch (e: Exception) {
                        logEventHandleErr(event, e, rebarItemOffHand)
                    }
                }
            }
        }
    }

    @EventHandler
    private fun handle(event: EntityDeathEvent) {
        val killer = event.damageSource.causingEntity
        if (killer is Player) {
            val rebarItemMainHand = RebarItem.fromStack(killer.inventory.itemInMainHand)
            if (rebarItemMainHand != null && !killer.canUse(rebarItemMainHand, true)) {
                event.isCancelled = true
                return
            }
            if (rebarItemMainHand is RebarWeapon) {
                try {
                    rebarItemMainHand.onUsedToKillEntity(event)
                } catch (e: Exception) {
                    logEventHandleErr(event, e, rebarItemMainHand)
                }
            }

            val rebarItemOffHand = RebarItem.fromStack(killer.inventory.itemInOffHand)
            if (rebarItemOffHand != null && !killer.canUse(rebarItemOffHand, true)) {
                event.isCancelled = true
                return
            }
            if (rebarItemOffHand is RebarWeapon) {
                try {
                    rebarItemOffHand.onUsedToKillEntity(event)
                } catch (e: Exception) {
                    logEventHandleErr(event, e, rebarItemOffHand)
                }
            }
        }
    }

    @EventHandler
    private fun handle(event: PlayerPickItemEvent) {
        val reachDistance = event.player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE)?.value ?: 4.5
        val block = event.player.getTargetBlockExact(ceil(reachDistance).toInt()) ?: return
        val pylonBlock = BlockStorage.get(block) ?: return
        val blockItem = pylonBlock.getPickItem() ?: return
        val blockRebarItem = RebarItem.fromStack(blockItem) ?: return

        val sourceSlot = event.sourceSlot
        if (sourceSlot != -1) {
            val sourceItem = event.player.inventory.getItem(event.sourceSlot)
            if (sourceItem != null) {
                val sourceRebarItem = RebarItem.fromStack(sourceItem)
                if (sourceRebarItem != null) {
                    // The source item is already of the correct Rebar type, so we shouldn't interfere with the event
                    return
                }
            }
        }

        // If we reach this point, the source item is not of the correct type
        // So we're going to search the inventory for a block of the correct type
        val existingSlot = findRebarItemInInventory(event.player.inventory, blockRebarItem)
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
        if (event.player.gameMode == GameMode.CREATIVE) {
            if (event.player.inventory.addItem(blockItem).isNotEmpty()) {
                // Inventory full, can't pick the item
                event.isCancelled = true
                return
            }
        }

        val newSourceSlot = findRebarItemInInventory(event.player.inventory, blockRebarItem)
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
    internal fun logEventHandleErr(event: Event, e: Exception, item: RebarItem) {
        Rebar.logger.severe("Error when handling item(${item.key}) event handler ${event.javaClass.simpleName}: ${e.localizedMessage}")
        e.printStackTrace()
    }
}