package io.github.pylonmc.pylon.core.recipe

import org.bukkit.Keyed
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack

interface MobDropRecipe : Keyed {

    fun getResult(entity: LivingEntity, playerKill: Boolean): ItemStack?
}