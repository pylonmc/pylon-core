package io.github.pylonmc.pylon.core.entity

import com.destroystokyo.paper.event.entity.*
import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.block.BlockStorage
import io.github.pylonmc.pylon.core.block.base.PylonHopper
import io.github.pylonmc.pylon.core.config.PylonConfig
import io.github.pylonmc.pylon.core.entity.base.PylonDeathEntity
import io.github.pylonmc.pylon.core.entity.base.PylonInteractEntity
import io.github.pylonmc.pylon.core.entity.base.PylonUnloadEntity
import io.github.pylonmc.pylon.core.entity.base.*
import io.github.pylonmc.pylon.core.event.PylonEntityDeathEvent
import io.github.pylonmc.pylon.core.event.PylonEntityUnloadEvent
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.item.PylonItemListener.logEventHandleErr
import io.github.pylonmc.pylon.core.item.base.PylonArrow
import io.github.pylonmc.pylon.core.item.base.PylonLingeringPotion
import io.github.pylonmc.pylon.core.item.base.PylonSplashPotion
import io.papermc.paper.event.entity.*
import io.papermc.paper.event.entity.EntityKnockbackEvent
import org.bukkit.block.Hopper
import org.bukkit.entity.AbstractArrow
import org.bukkit.entity.minecart.HopperMinecart
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.*
import org.bukkit.event.inventory.InventoryPickupItemEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import java.util.*

internal object EntityListener : Listener {
    private val entityErrMap: MutableMap<UUID, Int> = mutableMapOf()

    @EventHandler(priority = EventPriority.MONITOR)
    private fun handle(event: PlayerInteractEntityEvent) {
        val pylonEntity = EntityStorage.get(event.rightClicked)
        if (pylonEntity is PylonInteractEntity) {
            try {
                pylonEntity.onInteract(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonEntity)
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun handle(event: PylonEntityUnloadEvent) {
        if (event.pylonEntity is PylonUnloadEntity) {
            try {
                event.pylonEntity.onUnload(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, event.pylonEntity)
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun handle(event: PylonEntityDeathEvent) {
        if (event.pylonEntity is PylonDeathEntity) {
            try {
                event.pylonEntity.onDeath(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, event.pylonEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: ProjectileHitEvent) {
        if (event.entity is AbstractArrow) {
            val arrowItem = PylonItem.fromStack((event.entity as AbstractArrow).itemStack)
            if (arrowItem is PylonArrow) {
                try {
                    arrowItem.onArrowHit(event)
                } catch (e: Exception) {
                    logEventHandleErr(event, e, arrowItem)
                }
            }
        }
        val pylonEntity = EntityStorage.get(event.entity)
        if (pylonEntity is PylonProjectile) {
            try {
                pylonEntity.onHit(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonEntity)
            }
        }
    }

    @EventHandler
    private fun onInventoryPickup(event: InventoryPickupItemEvent) {
        val inv = event.inventory
        val holder = inv.holder
        if (holder is HopperMinecart) {
            val pyEntity = EntityStorage.get(holder.entity) as? PylonHopper ?: return
            pyEntity.onHopperPickUpItem(event)
        }
    }

    @EventHandler
    private fun handle(event: EntityDamageByEntityEvent) {
        if (event.damager is AbstractArrow) {
            val arrowItem = PylonItem.fromStack((event.damager as AbstractArrow).itemStack)
            if (arrowItem is PylonArrow) {
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
        val pylonPotion = PylonItem.fromStack(event.potion.item)
        if (pylonPotion is PylonSplashPotion) {
            try {
                pylonPotion.onSplash(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonPotion)
            }
        }
    }

    @EventHandler
    private fun handle(event: LingeringPotionSplashEvent) {
        val pylonPotion = PylonItem.fromStack(event.entity.item)
        if (pylonPotion is PylonLingeringPotion) {
            try {
                pylonPotion.onSplash(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonPotion)
            }
        }
    }

    @EventHandler
    private fun handle(event: CreeperIgniteEvent) {
        val pylonEntity = EntityStorage.get(event.entity)
        if (pylonEntity is PylonCreeper) {
            try {
                pylonEntity.onIgnite(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: CreeperPowerEvent) {
        val pylonEntity = EntityStorage.get(event.entity)
        if (pylonEntity is PylonCreeper) {
            try {
                pylonEntity.onPower(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: EnderDragonChangePhaseEvent) {
        val pylonEntity = EntityStorage.get(event.entity)
        if (pylonEntity is PylonEnderDragon) {
            try {
                pylonEntity.onChangePhase(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: EnderDragonFlameEvent) {
        val pylonEntity = EntityStorage.get(event.entity)
        if (pylonEntity is PylonEnderDragon) {
            try {
                pylonEntity.onFlame(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: EnderDragonFireballHitEvent) {
        val pylonEntity = EntityStorage.get(event.entity)
        if (pylonEntity is PylonDragonFireball) {
            try {
                pylonEntity.onHit(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: EnderDragonShootFireballEvent) {
        val pylonEntity = EntityStorage.get(event.entity)
        if (pylonEntity is PylonEnderDragon) {
            try {
                pylonEntity.onShootFireball(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: BatToggleSleepEvent) {
        val pylonEntity = EntityStorage.get(event.entity)
        if (pylonEntity is PylonBat) {
            try {
                pylonEntity.onToggleSleep(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: EndermanAttackPlayerEvent) {
        val pylonEntity = EntityStorage.get(event.entity)
        if (pylonEntity is PylonEnderman) {
            try {
                pylonEntity.onAttackPlayer(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: EndermanEscapeEvent) {
        val pylonEntity = EntityStorage.get(event.entity)
        if (pylonEntity is PylonEnderman) {
            try {
                pylonEntity.onEscape(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: EntityBreedEvent) {
        val pylonEntity = EntityStorage.get(event.entity)
        if (pylonEntity is PylonBreedable) {
            try {
                pylonEntity.onBreed(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: EntityEnterLoveModeEvent) {
        val pylonEntity = EntityStorage.get(event.entity)
        if (pylonEntity is PylonBreedable) {
            try {
                pylonEntity.onEnterLoveMode(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: EntityBreakDoorEvent) {
        val pylonEntity = EntityStorage.get(event.entity)
        if (pylonEntity is PylonCop) {
            try {
                pylonEntity.kickDoor(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: EntityCombustEvent) {
        val pylonEntity = EntityStorage.get(event.entity)
        if (pylonEntity is PylonCombustibleEntity) {
            try {
                pylonEntity.onCombust(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: EntityDyeEvent) {
        val pylonEntity = EntityStorage.get(event.entity)
        if (pylonEntity is PylonDyeable) {
            try {
                pylonEntity.onDye(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: EntityPathfindEvent) {
        val pylonEntity = EntityStorage.get(event.entity)
        if (pylonEntity is PylonPathingEntity) {
            try {
                pylonEntity.onFindPath(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: EntityTargetEvent) {
        val pylonEntity = EntityStorage.get(event.entity)
        if (pylonEntity is PylonPathingEntity) {
            try {
                pylonEntity.onTarget(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: EntityMoveEvent) {
        val pylonEntity = EntityStorage.get(event.entity)
        if (pylonEntity is PylonMovingEntity) {
            try {
                pylonEntity.onMove(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: EntityJumpEvent) {
        val pylonEntity = EntityStorage.get(event.entity)
        if (pylonEntity is PylonMovingEntity) {
            try {
                pylonEntity.onJump(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: EntityKnockbackEvent) {
        val pylonEntity = EntityStorage.get(event.entity)
        if (pylonEntity is PylonMovingEntity) {
            try {
                pylonEntity.onKnockback(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: EntityToggleSwimEvent) {
        val pylonEntity = EntityStorage.get(event.entity)
        if (pylonEntity is PylonMovingEntity) {
            try {
                pylonEntity.onToggleSwim(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: EntityToggleGlideEvent) {
        val pylonEntity = EntityStorage.get(event.entity)
        if (pylonEntity is PylonMovingEntity) {
            try {
                pylonEntity.onToggleGlide(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: EntityToggleSitEvent) {
        val pylonEntity = EntityStorage.get(event.entity)
        if (pylonEntity is PylonMovingEntity) {
            try {
                pylonEntity.onToggleSit(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: EntityDamageEvent) {
        val pylonEntity = EntityStorage.get(event.entity)
        if (pylonEntity is PylonDamageableEntity) {
            try {
                pylonEntity.onDamage(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: EntityRegainHealthEvent) {
        val pylonEntity = EntityStorage.get(event.entity)
        if (pylonEntity is PylonDamageableEntity) {
            try {
                pylonEntity.onRegainHealth(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: EntityMountEvent) {
        val mount = EntityStorage.get(event.mount)
        if (mount is PylonMountableEntity) {
            try {
                mount.onMount(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, mount)
            }
        }
        val mounter = EntityStorage.get(event.entity)
        if (mounter is PylonMountingEntity) {
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
        if (mount is PylonMountableEntity) {
            try {
                mount.onDismount(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, mount)
            }
        }
        val dismounter = EntityStorage.get(event.entity)
        if (dismounter is PylonMountingEntity) {
            try {
                dismounter.onDismount(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, dismounter)
            }
        }
    }

    @EventHandler
    private fun handle(event: EntitySpellCastEvent) {
        val pylonEntity = EntityStorage.get(event.entity)
        if (pylonEntity is PylonSpellcaster) {
            try {
                pylonEntity.onCastSpell(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: EntityResurrectEvent) {
        val pylonEntity = EntityStorage.get(event.entity)
        if (pylonEntity is PylonResurrectable) {
            try {
                pylonEntity.onResurrect(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: EntityTameEvent) {
        val pylonEntity = EntityStorage.get(event.entity)
        if (pylonEntity is PylonTameable) {
            try {
                pylonEntity.onTamed(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: TameableDeathMessageEvent) {
        val pylonEntity = EntityStorage.get(event.entity)
        if (pylonEntity is PylonTameable) {
            try {
                pylonEntity.onDeath(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: PlayerLeashEntityEvent) {
        val pylonEntity = EntityStorage.get(event.entity)
        if (pylonEntity is PylonLeashable) {
            try {
                pylonEntity.onLeash(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: EntityUnleashEvent) {
        val pylonEntity = EntityStorage.get(event.entity)
        if (pylonEntity is PylonLeashable) {
            try {
                pylonEntity.onUnleash(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: ItemDespawnEvent) {
        val pylonEntity = EntityStorage.get(event.entity)
        if (pylonEntity is PylonItemEntity) {
            try {
                pylonEntity.onDespawn(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: ItemMergeEvent) {
        val pylonEntity = EntityStorage.get(event.entity)
        if (pylonEntity is PylonItemEntity) {
            try {
                pylonEntity.onMerge(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: PiglinBarterEvent) {
        val pylonEntity = EntityStorage.get(event.entity)
        if (pylonEntity is PylonPiglin) {
            try {
                pylonEntity.onBarter(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: PigZombieAngerEvent) {
        val pylonEntity = EntityStorage.get(event.entity)
        if (pylonEntity is PylonZombiePigman) {
            try {
                pylonEntity.onAnger(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: TurtleStartDiggingEvent) {
        val pylonEntity = EntityStorage.get(event.entity)
        if (pylonEntity is PylonTurtle) {
            try {
                pylonEntity.onStartDigging(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: TurtleGoHomeEvent) {
        val pylonEntity = EntityStorage.get(event.entity)
        if (pylonEntity is PylonTurtle) {
            try {
                pylonEntity.onGoHome(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: TurtleLayEggEvent) {
        val pylonEntity = EntityStorage.get(event.entity)
        if (pylonEntity is PylonTurtle) {
            try {
                pylonEntity.onLayEgg(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: VillagerAcquireTradeEvent) {
        val pylonEntity = EntityStorage.get(event.entity)
        if (pylonEntity is PylonVillager) {
            try {
                pylonEntity.onAcquireTrade(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: VillagerCareerChangeEvent) {
        val pylonEntity = EntityStorage.get(event.entity)
        if (pylonEntity is PylonVillager) {
            try {
                pylonEntity.onCareerChange(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: VillagerReplenishTradeEvent) {
        val pylonEntity = EntityStorage.get(event.entity)
        if (pylonEntity is PylonVillager) {
            try {
                pylonEntity.onReplenishTrade(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: WitchConsumePotionEvent) {
        val pylonEntity = EntityStorage.get(event.entity)
        if (pylonEntity is PylonWitch) {
            try {
                pylonEntity.onConsumePotion(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: WitchReadyPotionEvent) {
        val pylonEntity = EntityStorage.get(event.entity)
        if (pylonEntity is PylonWitch) {
            try {
                pylonEntity.onReadyPotion(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: WitchThrowPotionEvent) {
        val pylonEntity = EntityStorage.get(event.entity)
        if (pylonEntity is PylonWitch) {
            try {
                pylonEntity.onThrowPotion(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: SlimeSwimEvent) {
        val pylonEntity = EntityStorage.get(event.entity)
        if (pylonEntity is PylonSlime) {
            try {
                pylonEntity.onSwim(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: SlimeSplitEvent) {
        val pylonEntity = EntityStorage.get(event.entity)
        if (pylonEntity is PylonSlime) {
            try {
                pylonEntity.onSplit(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: SlimeWanderEvent) {
        val pylonEntity = EntityStorage.get(event.entity)
        if (pylonEntity is PylonSlime) {
            try {
                pylonEntity.onWander(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: SlimePathfindEvent) {
        val pylonEntity = EntityStorage.get(event.entity)
        if (pylonEntity is PylonSlime) {
            try {
                pylonEntity.onPathfind(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: SlimeChangeDirectionEvent) {
        val pylonEntity = EntityStorage.get(event.entity)
        if (pylonEntity is PylonSlime) {
            try {
                pylonEntity.onChangeDirection(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: SlimeTargetLivingEntityEvent) {
        val pylonEntity = EntityStorage.get(event.entity)
        if (pylonEntity is PylonSlime) {
            try {
                pylonEntity.onTarget(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: FireworkExplodeEvent) {
        val pylonEntity = EntityStorage.get(event.entity)
        if (pylonEntity is PylonFirework) {
            pylonEntity.onExplode(event)
        }
    }

    @EventHandler
    private fun handle(event: ExplosionPrimeEvent) {
        val pylonEntity = EntityStorage.get(event.entity)
        if (pylonEntity is PylonExplosiveEntity) {
            try {
                pylonEntity.onPrime(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: ExperienceOrbMergeEvent) {
        val source = EntityStorage.get(event.mergeSource)
        if (source is PylonExperienceOrb) {
            try {
                source.onMergeOrb(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, source)
            }
        }
        val target = EntityStorage.get(event.mergeTarget)
        if (target is PylonExperienceOrb) {
            try {
                target.onAbsorbedByOrb(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, target)
            }
        }
    }

    @JvmSynthetic
    internal fun logEventHandleErr(event: Event?, e: Exception, entity: PylonEntity<*>) {
        PylonCore.logger.severe("Error when handling entity(${entity.key}, ${entity.uuid}, ${entity.entity.location}) event handler ${event?.javaClass?.simpleName}: ${e.localizedMessage}")
        e.printStackTrace()
        entityErrMap[entity.uuid] = entityErrMap[entity.uuid]?.plus(1) ?: 1
        if (entityErrMap[entity.uuid]!! > PylonConfig.ALLOWED_ENTITY_ERRORS) {
            entity.entity.remove()
        }
    }

    @EventHandler
    private fun onEntityUnload(event: PylonEntityUnloadEvent) {
        entityErrMap.remove(event.pylonEntity.uuid)
    }
}