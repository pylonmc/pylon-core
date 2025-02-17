package io.github.pylonmc.pylon.core.mobdrop

import org.bukkit.Keyed
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.inventory.ItemStack

interface MobDrop : Keyed {

    fun getResult(event: EntityDeathEvent): ItemStack?
}