package io.github.pylonmc.pylon.core.mobdrop

import org.bukkit.Keyed
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.inventory.ItemStack

/**
 * To use, register it with [PylonRegistry.MOB_DROPS]
 */
interface MobDrop : Keyed {

    fun getResult(event: EntityDeathEvent): ItemStack?
}