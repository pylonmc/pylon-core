package io.github.pylonmc.pylon.base.content.tools.base

import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.item.base.PylonInventoryItem
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitTask
import java.util.*

abstract class InventoryEffectItem(stack: ItemStack) : PylonItem(stack), PylonInventoryItem {
    override fun onTick(player: Player, stack: ItemStack) {
        tasks.putIfAbsent(effectKey, WeakHashMap())
        tasks[effectKey]!![player.uniqueId]?.cancel()
        if(!player.persistentDataContainer.has(effectKey)){
            player.persistentDataContainer.set(effectKey, PersistentDataType.BOOLEAN, true)
            applyEffect(player, stack)
        }
        tasks[effectKey]!![player.uniqueId] = Bukkit.getScheduler().runTaskLater(PylonCore.javaPlugin, Runnable {
            player.persistentDataContainer.remove(effectKey)
            removeEffect(player, stack) }, tickSpeed.tickRate)
    }

    /**
     * Remove the effect from the player.
     */
    protected abstract fun removeEffect(player: Player, stack: ItemStack)

    /**
     * Apply the effect of this item onto the player
     */
    protected abstract fun applyEffect(player: Player, stack: ItemStack)

    private val effectKey = NamespacedKey(key.namespace, key.key + "_effect")

    companion object {
        private val tasks = HashMap<NamespacedKey, WeakHashMap<UUID, BukkitTask>>()
    }
}