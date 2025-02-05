package io.github.pylonmc.pylon.core.recipe

import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack
import java.util.concurrent.ThreadLocalRandom

@FunctionalInterface
fun interface MobDropRecipe {

    fun getResult(entity: LivingEntity, playerKill: Boolean): ItemStack?

    data class Simple @JvmOverloads constructor(
        val result: ItemStack,
        val mob: EntityType,
        val playerKill: Boolean,
        val chance: Double = 1.0,
    ) : MobDropRecipe {
        override fun getResult(entity: LivingEntity, playerKill: Boolean): ItemStack? {
            return if (
                entity.type == mob
                && (playerKill == this.playerKill || !this.playerKill)
                && ThreadLocalRandom.current().nextDouble() <= chance
            ) {
                result
            } else {
                null
            }
        }
    }
}