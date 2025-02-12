package io.github.pylonmc.pylon.core.recipe

import org.bukkit.NamespacedKey
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack
import java.util.concurrent.ThreadLocalRandom

class SimpleMobDrop @JvmOverloads constructor(
    private val key: NamespacedKey,
    private val result: ItemStack,
    val mob: EntityType,
    val playerKill: Boolean,
    val chance: Float = 1f,
) : MobDrop {
    override fun getResult(entity: LivingEntity, playerKill: Boolean): ItemStack? {
        return if (
            entity.type == mob
            && (playerKill == this.playerKill || !this.playerKill)
            && ThreadLocalRandom.current().nextFloat() <= chance
        ) {
            result.clone()
        } else {
            null
        }
    }

    override fun getKey(): NamespacedKey = key
}