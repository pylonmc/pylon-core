package io.github.pylonmc.pylon.core.mobdrop

import org.bukkit.NamespacedKey
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.inventory.ItemStack
import java.util.concurrent.ThreadLocalRandom

open class SimpleMobDrop @JvmOverloads constructor(
    private val key: NamespacedKey,
    private val result: ItemStack,
    val mob: EntityType,
    val playerKill: Boolean,
    val chance: Float = 1f,
) : MobDrop {

    @Suppress("UnstableApiUsage") // DamageSource is unstable
    override fun getResult(event: EntityDeathEvent): ItemStack? {
        return if (
            event.entity.type == mob
            && (event.damageSource.causingEntity is Player == this.playerKill || !this.playerKill)
            && ThreadLocalRandom.current().nextFloat() <= chance
        ) {
            result.clone()
        } else {
            null
        }
    }

    override fun getKey(): NamespacedKey = key
}