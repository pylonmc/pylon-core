package io.github.pylonmc.rebar.entity

import com.destroystokyo.paper.event.entity.*
import io.github.pylonmc.rebar.Rebar
import io.github.pylonmc.rebar.block.BlockListener
import io.github.pylonmc.rebar.block.base.RebarHopper
import io.github.pylonmc.rebar.config.RebarConfig
import io.github.pylonmc.rebar.entity.base.*
import io.github.pylonmc.rebar.event.RebarEntityDeathEvent
import io.github.pylonmc.rebar.event.RebarEntityUnloadEvent
import io.github.pylonmc.rebar.item.RebarItem
import io.github.pylonmc.rebar.item.RebarItemListener.logEventHandleErr
import io.github.pylonmc.rebar.item.base.RebarArrow
import io.github.pylonmc.rebar.item.base.RebarLingeringPotion
import io.github.pylonmc.rebar.item.base.RebarSplashPotion
import io.papermc.paper.event.entity.*
import io.papermc.paper.event.entity.EntityKnockbackEvent
import org.bukkit.entity.AbstractArrow
import org.bukkit.entity.minecart.HopperMinecart
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.*
import org.bukkit.event.inventory.InventoryPickupItemEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import java.util.UUID

internal object EntityListener : Listener {
    private val entityErrMap: MutableMap<UUID, Int> = mutableMapOf()

    @EventHandler(priority = EventPriority.MONITOR)
    private fun handle(event: PlayerInteractEntityEvent) {
        val rebarEntity = EntityStorage.get(event.rightClicked)
        if (rebarEntity is RebarInteractEntity) {
            try {
                rebarEntity.onInteract(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarEntity)
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun handle(event: RebarEntityUnloadEvent) {
        if (event.rebarEntity is RebarUnloadEntity) {
            try {
                event.rebarEntity.onUnload(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, event.rebarEntity)
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun handle(event: RebarEntityDeathEvent) {
        if (event.rebarEntity is RebarDeathEntity) {
            try {
                event.rebarEntity.onDeath(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, event.rebarEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: ProjectileHitEvent) {
        if (event.entity is AbstractArrow) {
            val arrowItem = RebarItem.fromStack((event.entity as AbstractArrow).itemStack)
            if (arrowItem is RebarArrow) {
                try {
                    arrowItem.onArrowHit(event)
                } catch (e: Exception) {
                    logEventHandleErr(event, e, arrowItem)
                }
            }
        }
        val rebarEntity = EntityStorage.get(event.entity)
        if (rebarEntity is RebarProjectile) {
            try {
                rebarEntity.onHit(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarEntity)
            }
        }
    }

    @EventHandler
    private fun onInventoryPickup(event: InventoryPickupItemEvent) {
        val inv = event.inventory
        val holder = inv.holder
        if (holder is HopperMinecart) {
            val pyEntity = EntityStorage.get(holder.entity) as? RebarHopper ?: return
            pyEntity.onHopperPickUpItem(event, EventPriority.NORMAL)
        }
    }

    @EventHandler
    private fun handle(event: EntityDamageByEntityEvent) {
        if (event.damager is AbstractArrow) {
            val arrowItem = RebarItem.fromStack((event.damager as AbstractArrow).itemStack)
            if (arrowItem is RebarArrow) {
                try {
                    arrowItem.onArrowDamage(event)
                } catch (e: Exception) {
                    logEventHandleErr(event, e, arrowItem)
                }
            }
        }
    }

    @EventHandler
    private fun handle(event: PotionSplashEvent) {
        val rebarPotion = RebarItem.fromStack(event.potion.item)
        if (rebarPotion is RebarSplashPotion) {
            try {
                rebarPotion.onSplash(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarPotion)
            }
        }
    }

    @EventHandler
    private fun handle(event: LingeringPotionSplashEvent) {
        val rebarPotion = RebarItem.fromStack(event.entity.item)
        if (rebarPotion is RebarLingeringPotion) {
            try {
                rebarPotion.onSplash(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarPotion)
            }
        }
    }

    @EventHandler
    private fun handle(event: CreeperIgniteEvent) {
        val rebarEntity = EntityStorage.get(event.entity)
        if (rebarEntity is RebarCreeper) {
            try {
                rebarEntity.onIgnite(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: CreeperPowerEvent) {
        val rebarEntity = EntityStorage.get(event.entity)
        if (rebarEntity is RebarCreeper) {
            try {
                rebarEntity.onPower(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: EnderDragonChangePhaseEvent) {
        val rebarEntity = EntityStorage.get(event.entity)
        if (rebarEntity is RebarEnderDragon) {
            try {
                rebarEntity.onChangePhase(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: EnderDragonFlameEvent) {
        val rebarEntity = EntityStorage.get(event.entity)
        if (rebarEntity is RebarEnderDragon) {
            try {
                rebarEntity.onFlame(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: EnderDragonFireballHitEvent) {
        val rebarEntity = EntityStorage.get(event.entity)
        if (rebarEntity is RebarDragonFireball) {
            try {
                rebarEntity.onHit(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: EnderDragonShootFireballEvent) {
        val rebarEntity = EntityStorage.get(event.entity)
        if (rebarEntity is RebarEnderDragon) {
            try {
                rebarEntity.onShootFireball(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: BatToggleSleepEvent) {
        val rebarEntity = EntityStorage.get(event.entity)
        if (rebarEntity is RebarBat) {
            try {
                rebarEntity.onToggleSleep(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: EndermanAttackPlayerEvent) {
        val rebarEntity = EntityStorage.get(event.entity)
        if (rebarEntity is RebarEnderman) {
            try {
                rebarEntity.onAttackPlayer(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: EndermanEscapeEvent) {
        val rebarEntity = EntityStorage.get(event.entity)
        if (rebarEntity is RebarEnderman) {
            try {
                rebarEntity.onEscape(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: EntityBreedEvent) {
        val rebarEntity = EntityStorage.get(event.entity)
        if (rebarEntity is RebarBreedable) {
            try {
                rebarEntity.onBreed(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: EntityEnterLoveModeEvent) {
        val rebarEntity = EntityStorage.get(event.entity)
        if (rebarEntity is RebarBreedable) {
            try {
                rebarEntity.onEnterLoveMode(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: EntityBreakDoorEvent) {
        val rebarEntity = EntityStorage.get(event.entity)
        if (rebarEntity is RebarCop) {
            try {
                rebarEntity.kickDoor(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: EntityCombustEvent) {
        val rebarEntity = EntityStorage.get(event.entity)
        if (rebarEntity is RebarCombustibleEntity) {
            try {
                rebarEntity.onCombust(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: EntityDyeEvent) {
        val rebarEntity = EntityStorage.get(event.entity)
        if (rebarEntity is RebarDyeable) {
            try {
                rebarEntity.onDye(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: EntityPathfindEvent) {
        val rebarEntity = EntityStorage.get(event.entity)
        if (rebarEntity is RebarPathingEntity) {
            try {
                rebarEntity.onFindPath(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: EntityTargetEvent) {
        val rebarEntity = EntityStorage.get(event.entity)
        if (rebarEntity is RebarPathingEntity) {
            try {
                rebarEntity.onTarget(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: EntityMoveEvent) {
        val rebarEntity = EntityStorage.get(event.entity)
        if (rebarEntity is RebarMovingEntity) {
            try {
                rebarEntity.onMove(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: EntityJumpEvent) {
        val rebarEntity = EntityStorage.get(event.entity)
        if (rebarEntity is RebarMovingEntity) {
            try {
                rebarEntity.onJump(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: EntityKnockbackEvent) {
        val rebarEntity = EntityStorage.get(event.entity)
        if (rebarEntity is RebarMovingEntity) {
            try {
                rebarEntity.onKnockback(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: EntityToggleSwimEvent) {
        val rebarEntity = EntityStorage.get(event.entity)
        if (rebarEntity is RebarMovingEntity) {
            try {
                rebarEntity.onToggleSwim(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: EntityToggleGlideEvent) {
        val rebarEntity = EntityStorage.get(event.entity)
        if (rebarEntity is RebarMovingEntity) {
            try {
                rebarEntity.onToggleGlide(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: EntityToggleSitEvent) {
        val rebarEntity = EntityStorage.get(event.entity)
        if (rebarEntity is RebarMovingEntity) {
            try {
                rebarEntity.onToggleSit(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: EntityDamageEvent) {
        val rebarEntity = EntityStorage.get(event.entity)
        if (rebarEntity is RebarDamageableEntity) {
            try {
                rebarEntity.onDamage(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: EntityRegainHealthEvent) {
        val rebarEntity = EntityStorage.get(event.entity)
        if (rebarEntity is RebarDamageableEntity) {
            try {
                rebarEntity.onRegainHealth(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: EntityMountEvent) {
        val mount = EntityStorage.get(event.mount)
        if (mount is RebarMountableEntity) {
            try {
                mount.onMount(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, mount)
            }
        }
        val mounter = EntityStorage.get(event.entity)
        if (mounter is RebarMountingEntity) {
            try {
                mounter.onMount(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, mounter)
            }
        }
    }

    @EventHandler
    private fun handle(event: EntityDismountEvent) {
        val mount = EntityStorage.get(event.dismounted)
        if (mount is RebarMountableEntity) {
            try {
                mount.onDismount(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, mount)
            }
        }
        val dismounter = EntityStorage.get(event.entity)
        if (dismounter is RebarMountingEntity) {
            try {
                dismounter.onDismount(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, dismounter)
            }
        }
    }

    @EventHandler
    private fun handle(event: EntitySpellCastEvent) {
        val rebarEntity = EntityStorage.get(event.entity)
        if (rebarEntity is RebarSpellcaster) {
            try {
                rebarEntity.onCastSpell(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: EntityResurrectEvent) {
        val rebarEntity = EntityStorage.get(event.entity)
        if (rebarEntity is RebarResurrectable) {
            try {
                rebarEntity.onResurrect(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: EntityTameEvent) {
        val rebarEntity = EntityStorage.get(event.entity)
        if (rebarEntity is RebarTameable) {
            try {
                rebarEntity.onTamed(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: TameableDeathMessageEvent) {
        val rebarEntity = EntityStorage.get(event.entity)
        if (rebarEntity is RebarTameable) {
            try {
                rebarEntity.onDeath(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: PlayerLeashEntityEvent) {
        val rebarEntity = EntityStorage.get(event.entity)
        if (rebarEntity is RebarLeashable) {
            try {
                rebarEntity.onLeash(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: EntityUnleashEvent) {
        val rebarEntity = EntityStorage.get(event.entity)
        if (rebarEntity is RebarLeashable) {
            try {
                rebarEntity.onUnleash(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: ItemDespawnEvent) {
        val rebarEntity = EntityStorage.get(event.entity)
        if (rebarEntity is RebarItemEntity) {
            try {
                rebarEntity.onDespawn(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: ItemMergeEvent) {
        val rebarEntity = EntityStorage.get(event.entity)
        if (rebarEntity is RebarItemEntity) {
            try {
                rebarEntity.onMerge(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: PiglinBarterEvent) {
        val rebarEntity = EntityStorage.get(event.entity)
        if (rebarEntity is RebarPiglin) {
            try {
                rebarEntity.onBarter(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: PigZombieAngerEvent) {
        val rebarEntity = EntityStorage.get(event.entity)
        if (rebarEntity is RebarZombiePigman) {
            try {
                rebarEntity.onAnger(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: TurtleStartDiggingEvent) {
        val rebarEntity = EntityStorage.get(event.entity)
        if (rebarEntity is RebarTurtle) {
            try {
                rebarEntity.onStartDigging(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: TurtleGoHomeEvent) {
        val rebarEntity = EntityStorage.get(event.entity)
        if (rebarEntity is RebarTurtle) {
            try {
                rebarEntity.onGoHome(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: TurtleLayEggEvent) {
        val rebarEntity = EntityStorage.get(event.entity)
        if (rebarEntity is RebarTurtle) {
            try {
                rebarEntity.onLayEgg(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: VillagerAcquireTradeEvent) {
        val rebarEntity = EntityStorage.get(event.entity)
        if (rebarEntity is RebarVillager) {
            try {
                rebarEntity.onAcquireTrade(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: VillagerCareerChangeEvent) {
        val rebarEntity = EntityStorage.get(event.entity)
        if (rebarEntity is RebarVillager) {
            try {
                rebarEntity.onCareerChange(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: VillagerReplenishTradeEvent) {
        val rebarEntity = EntityStorage.get(event.entity)
        if (rebarEntity is RebarVillager) {
            try {
                rebarEntity.onReplenishTrade(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: WitchConsumePotionEvent) {
        val rebarEntity = EntityStorage.get(event.entity)
        if (rebarEntity is RebarWitch) {
            try {
                rebarEntity.onConsumePotion(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: WitchReadyPotionEvent) {
        val rebarEntity = EntityStorage.get(event.entity)
        if (rebarEntity is RebarWitch) {
            try {
                rebarEntity.onReadyPotion(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: WitchThrowPotionEvent) {
        val rebarEntity = EntityStorage.get(event.entity)
        if (rebarEntity is RebarWitch) {
            try {
                rebarEntity.onThrowPotion(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: SlimeSwimEvent) {
        val rebarEntity = EntityStorage.get(event.entity)
        if (rebarEntity is RebarSlime) {
            try {
                rebarEntity.onSwim(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: SlimeSplitEvent) {
        val rebarEntity = EntityStorage.get(event.entity)
        if (rebarEntity is RebarSlime) {
            try {
                rebarEntity.onSplit(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: SlimeWanderEvent) {
        val rebarEntity = EntityStorage.get(event.entity)
        if (rebarEntity is RebarSlime) {
            try {
                rebarEntity.onWander(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: SlimePathfindEvent) {
        val rebarEntity = EntityStorage.get(event.entity)
        if (rebarEntity is RebarSlime) {
            try {
                rebarEntity.onPathfind(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: SlimeChangeDirectionEvent) {
        val rebarEntity = EntityStorage.get(event.entity)
        if (rebarEntity is RebarSlime) {
            try {
                rebarEntity.onChangeDirection(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: SlimeTargetLivingEntityEvent) {
        val rebarEntity = EntityStorage.get(event.entity)
        if (rebarEntity is RebarSlime) {
            try {
                rebarEntity.onTarget(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: FireworkExplodeEvent) {
        val rebarEntity = EntityStorage.get(event.entity)
        if (rebarEntity is RebarFirework) {
            rebarEntity.onExplode(event)
        }
    }

    @EventHandler
    private fun handle(event: ExplosionPrimeEvent) {
        val rebarEntity = EntityStorage.get(event.entity)
        if (rebarEntity is RebarExplosiveEntity) {
            try {
                rebarEntity.onPrime(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, rebarEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: ExperienceOrbMergeEvent) {
        val source = EntityStorage.get(event.mergeSource)
        if (source is RebarExperienceOrb) {
            try {
                source.onMergeOrb(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, source)
            }
        }
        val target = EntityStorage.get(event.mergeTarget)
        if (target is RebarExperienceOrb) {
            try {
                target.onAbsorbedByOrb(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, target)
            }
        }
    }

    @JvmSynthetic
    internal fun logEventHandleErr(event: Event, e: Exception, entity: RebarEntity<*>) {
        Rebar.logger.severe("Error when handling entity(${entity.key}, ${entity.uuid}, ${entity.entity.location}) event handler ${event.javaClass.simpleName}: ${e.localizedMessage}")
        e.printStackTrace()
        entityErrMap[entity.uuid] = entityErrMap[entity.uuid]?.plus(1) ?: 1
        if (entityErrMap[entity.uuid]!! > RebarConfig.ALLOWED_ENTITY_ERRORS) {
            entity.entity.remove()
        }
    }

    @JvmSynthetic
    internal fun logEventHandleErrTicking(e: Exception, entity: RebarEntity<*>) {
        Rebar.logger.severe("Error when handling ticking entity(${entity.key}, ${entity.uuid}, ${entity.entity.location}): ${e.localizedMessage}")
        e.printStackTrace()
        entityErrMap[entity.uuid] = entityErrMap[entity.uuid]?.plus(1) ?: 1
        if (entityErrMap[entity.uuid]!! > RebarConfig.ALLOWED_ENTITY_ERRORS) {
            entity.entity.remove()
        }
    }

    @EventHandler
    private fun onEntityUnload(event: RebarEntityUnloadEvent) {
        entityErrMap.remove(event.rebarEntity.uuid)
    }
}