package io.github.pylonmc.pylon.core.item.base

import io.papermc.paper.event.block.BlockPreDispenseEvent
import org.bukkit.Effect
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.Dispenser
import org.bukkit.entity.AbstractArrow
import org.bukkit.entity.Egg
import org.bukkit.entity.Fireball
import org.bukkit.entity.Firework
import org.bukkit.entity.LingeringPotion
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.entity.SmallFireball
import org.bukkit.entity.Snowball
import org.bukkit.entity.ThrowableProjectile
import org.bukkit.entity.ThrownExpBottle
import org.bukkit.entity.ThrownPotion
import org.bukkit.entity.Trident
import org.bukkit.entity.WindCharge
import org.bukkit.event.Event
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockDispenseEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import javax.annotation.OverridingMethodsMustInvokeSuper
import kotlin.random.Random
import org.bukkit.block.data.type.Dispenser as DispenserData

/**
 * An item that can launch a custom projectile entity when used by a player or dispensed from a dispenser.
 * Use this **only** for items that cannot be launched as a projectile in vanilla Minecraft (e.g. don't use this for snowballs, eggs, splash potions, etc.),
 */
interface PylonCustomProjectile<T: Projectile>: PylonInteractor, PylonProjectile, PylonDispensable  {
    val stack: ItemStack

    /**
     * The class of the projectile entity that will be launched.
     */
    val projectileType: Class<T>
    /**
     * Whether to consume one item from the stack when the projectile is launched.
     */
    val consumeWhenLaunched: Boolean

    /**
     * Whether this projectile can be dispensed as an entity from a dispenser.
     */
    val dispensable: Boolean

    @OverridingMethodsMustInvokeSuper
    override fun onUsedToRightClick(event: PlayerInteractEvent) {
        if (!event.action.isRightClick || (event.useInteractedBlock() == Event.Result.ALLOW && !event.player.isSneaking) || event.useItemInHand() == Event.Result.DENY) return

        val player = event.player
        try {
            val projectileStack = stack.asOne()
            val projectile = player.launchProjectile(projectileType, playerLaunchVelocity(player)) {
                when (it) {
                    is ThrowableProjectile -> it.item = projectileStack
                    is AbstractArrow -> it.itemStack = projectileStack
                    else -> throw IllegalStateException("Unsupported projectile type: ${projectileType.name}")
                }
            }

            if (projectile.isValid) {
                playerLaunchEffect(player, projectile)
                player.swingHand(event.hand ?: throw IllegalStateException("Hand is null"))
                if (consumeWhenLaunched && player.gameMode != GameMode.CREATIVE) {
                    stack.subtract()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @OverridingMethodsMustInvokeSuper
    override fun onPreDispense(event: BlockPreDispenseEvent) {
        val block = event.block
        if (!dispensable || simulatedEvents.contains(event) || event.isCancelled) return

        val data = block.blockData as? DispenserData ?: return
        val dispenser = block.state as? Dispenser ?: return
        val source = dispenser.blockProjectileSource ?: return
        event.isCancelled = true

        val preEvent = BlockPreDispenseEvent(block, event.itemStack, event.slot)
        simulatedEvents.add(preEvent)
        if (preEvent.callEvent()) {
            source.launchProjectile(projectileType, dispenserLaunchVelocity(block, data, dispenser)) {
                val dispenseEvent = BlockDispenseEvent(block, stack.asOne(), it.velocity)
                if (dispenseEvent.callEvent()) {
                    if (consumeWhenLaunched) stack.subtract()
                    when (it) {
                        is ThrowableProjectile -> it.item = dispenseEvent.item
                        is AbstractArrow -> it.itemStack = dispenseEvent.item
                        else -> throw IllegalStateException("Unsupported projectile type: ${projectileType.name}")
                    }
                    dispenserLaunchEffect(block, data, dispenser, it)
                } else {
                    it.remove()
                }
            }
        }
    }

    /**
     * The launch velocity of the projectile when launched by a player.
     * If null, the default player launch velocity will be used.
     */
    fun playerLaunchVelocity(player: Player): Vector? {
        return null
    }

    /**
     * The launch effect of the projectile when launched by a player.
     * By default, plays the sound corresponding to the vanilla projectile type.
     */
    fun playerLaunchEffect(player: Player, projectile: T) {
        val sound = when(projectileType) {
            AbstractArrow::class.java -> Sound.ENTITY_ARROW_SHOOT
            Egg::class.java -> Sound.ENTITY_EGG_THROW
            ThrownExpBottle::class.java -> Sound.ENTITY_EXPERIENCE_BOTTLE_THROW
            SmallFireball::class.java -> Sound.ENTITY_BLAZE_SHOOT
            Fireball::class.java -> Sound.ENTITY_GHAST_SHOOT
            Firework::class.java -> Sound.ENTITY_FIREWORK_ROCKET_LAUNCH
            LingeringPotion::class.java -> Sound.ENTITY_LINGERING_POTION_THROW
            Snowball::class.java -> Sound.ENTITY_SNOWBALL_THROW
            ThrownPotion::class.java -> Sound.ENTITY_SPLASH_POTION_THROW
            Trident::class.java -> Sound.ITEM_TRIDENT_THROW
            WindCharge::class.java -> Sound.ENTITY_WIND_CHARGE_THROW
            else -> Sound.ENTITY_ARROW_SHOOT
        }
        player.world.playSound(player, sound, 0.5f, 0.4F / (Random.Default.nextFloat() * 0.4F + 0.8F))
    }

    /**
     * The launch velocity of the projectile when launched by a dispenser.
     * If null, the default dispenser launch velocity will be used.
     */
    fun dispenserLaunchVelocity(block: Block, data: DispenserData, dispenser: Dispenser): Vector? {
        return null
    }

    /**
     * The launch effect of the projectile when launched by a dispenser.
     * By default, plays the effects corresponding to the vanilla projectile type. (smoke and sound)
     */
    fun dispenserLaunchEffect(block: Block, data: DispenserData, dispenser: Dispenser, projectile: T) {
        val effect = when(projectileType) {
            SmallFireball::class.java -> Effect.BLAZE_SHOOT
            Fireball::class.java -> Effect.BLAZE_SHOOT
            Firework::class.java -> Effect.FIREWORK_SHOOT
            WindCharge::class.java -> Effect.SOUND_WITH_CHARGE_SHOT
            else -> Effect.BOW_FIRE
        }
        block.world.playEffect(block.location, effect, 0)
        block.world.playEffect(block.location, Effect.SMOKE, data.facing)
    }

    companion object {
        private val simulatedEvents = mutableSetOf<BlockPreDispenseEvent>()
    }
}