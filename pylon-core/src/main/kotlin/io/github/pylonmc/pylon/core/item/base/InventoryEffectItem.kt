package io.github.pylonmc.pylon.core.item.base

import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.item.PylonItem
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitTask
import org.jetbrains.annotations.MustBeInvokedByOverriders
import java.util.*

interface InventoryEffectItem : PylonInventoryItem {
    override fun onTick(player: Player) {
        tasks.putIfAbsent(itemKey, HashMap())
        tasks[itemKey]!![player.uniqueId]?.cancel()
        if (!player.persistentDataContainer.has(itemKey)) {
            onAddedToInventory(player)
        }
        tasks[itemKey]!![player.uniqueId] = Bukkit.getScheduler().runTaskLater(PylonCore.javaPlugin, Runnable {
            onRemovedFromInventory(player)
        }, tickSpeed.tickRate)
    }

    /**
     * Remove the effect from the player. Best-effort removal therefore is no guarantee that the player is still connected or that the
     * [PylonItem.stack] is up to date with the actual ItemStack when it runs.
     */
    @MustBeInvokedByOverriders
    fun onRemovedFromInventory(player: Player) {
        player.persistentDataContainer.remove(itemKey)
    }

    /**
     * Apply the effect of this item onto the player
     */
    @MustBeInvokedByOverriders
    fun onAddedToInventory(player: Player) {
        player.persistentDataContainer.set(itemKey, PersistentDataType.BOOLEAN, true)
    }

    val itemKey: NamespacedKey
        get() = NamespacedKey((this as PylonItem).key.namespace, key.key + "_effect")

    companion object {
        private val tasks = HashMap<NamespacedKey, HashMap<UUID, BukkitTask>>()
    }
}