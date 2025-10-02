package io.github.pylonmc.pylon.core.particles

import com.destroystokyo.paper.ParticleBuilder
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.World
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.joml.Vector3d

/**
 * A builder for creating and spawning particles.
 *
 * Unlike [ParticleBuilder], this builder is type safe, and only allows valid configurations for each particle type.
 */
@Suppress("unused")
sealed class PylonParticleBuilder<S : PylonParticleBuilder<S>>(val innerBuilder: ParticleBuilder) {

    fun location(location: Location) = applySelf { innerBuilder.location(location) }
    fun location(world: World, x: Double, y: Double, z: Double) = applySelf { innerBuilder.location(world, x, y, z) }
    fun spawn() = innerBuilder.spawn()

    // @formatter:off
    fun allPlayers() = applySelf { innerBuilder.allPlayers() }
    fun receivers(players: Collection<Player>?) = applySelf { innerBuilder.receivers(players) }
    fun receivers(vararg players: Player) = applySelf { innerBuilder.receivers(*players) }
    @JvmOverloads
    fun receivers(radius: Int, spherical: Boolean = false) = applySelf { innerBuilder.receivers(radius, spherical) }
    @JvmOverloads
    fun receivers(xzRadius: Int, yRadius: Int, spherical: Boolean = false) = applySelf { innerBuilder.receivers(xzRadius, yRadius, spherical) }
    fun receivers(xRadius: Int, yRadius: Int, zRadius: Int) = applySelf { innerBuilder.receivers(xRadius, yRadius, zRadius) }
    fun force(force: Boolean) = applySelf { innerBuilder.force(force) }

    fun source(player: Player?) = applySelf { innerBuilder.source(player) }
    // @formatter:on

    protected inline fun applySelf(block: S.() -> Unit): S {
        @Suppress("UNCHECKED_CAST")
        block(this as S)
        return this
    }

    class Multiple(particle: Single<*>) : PylonParticleBuilder<Multiple>(particle.innerBuilder.clone()) {
        fun count(count: Int) = applySelf { innerBuilder.count(count) }
    }

    sealed class Single<S : Single<S>>(particle: Particle) : PylonParticleBuilder<S>(particle.builder()) {
        fun multiple() = Multiple(this)
    }

    sealed interface BehaviorBase<S : BehaviorBase<S>> {
        val innerBuilder: ParticleBuilder
    }

    @Suppress("UNCHECKED_CAST")
    sealed interface Offsettable<S : Offsettable<S>> : BehaviorBase<S> {
        fun offset(x: Double, y: Double, z: Double) = apply { innerBuilder.offset(x, y, z) } as S
    }

    @Suppress("UNCHECKED_CAST")
    sealed interface Directional<S : Directional<S>> : BehaviorBase<S> {
        fun velocity(velocity: Vector3d) = apply {
            innerBuilder.offset(velocity.x, velocity.y, velocity.z).extra(velocity.length())
        } as S
        fun velocity(x: Double, y: Double, z: Double) = velocity(Vector3d(x, y, z))
    }

    @Suppress("UNCHECKED_CAST")
    sealed interface Colored<S : Colored<S>> : BehaviorBase<S> {
        fun color(color: Color) = apply { innerBuilder.data(color) } as S
    }

    @Suppress("UNCHECKED_CAST")
    sealed interface Converging<S : Converging<S>> : BehaviorBase<S> {
        fun startOffset(x: Double, y: Double, z: Double) = apply { innerBuilder.offset(x, y, z) } as S
        fun startOffset(offset: Vector3d) = startOffset(offset.x, offset.y, offset.z)
    }

    @Suppress("UNCHECKED_CAST")
    sealed interface UsesBlockData<S : UsesBlockData<S>> : BehaviorBase<S> {
        fun blockData(blockData: BlockData) = apply { innerBuilder.data(blockData) } as S
    }

    @Suppress("UNCHECKED_CAST")
    sealed interface Rising<S : Rising<S>> : BehaviorBase<S> {
        fun initialVelocity(x: Double, y: Double, z: Double) = apply { innerBuilder.offset(x, y, z) } as S
        fun initialVelocity(velocity: Vector3d) = initialVelocity(velocity.x, velocity.y, velocity.z)
    }

    @Suppress("UNCHECKED_CAST")
    sealed interface Scalable<S : Scalable<S>> : BehaviorBase<S> {
        fun scale(scale: Double) = apply { innerBuilder.offset(scale, 0.0, 0.0) } as S
    }

    // @formatter:off
    class Type private constructor() {
        class AngryVillager : Single<AngryVillager>(Particle.ANGRY_VILLAGER), Offsettable<AngryVillager>
        class Ash : Single<Ash>(Particle.ASH), Offsettable<Ash>
        class Block : Single<Block>(Particle.BLOCK), Directional<Block>, UsesBlockData<Block>
        class BlockCrumble : Single<BlockCrumble>(Particle.BLOCK_CRUMBLE), Offsettable<BlockCrumble>, UsesBlockData<BlockCrumble>
        class BlockMarker : Single<BlockMarker>(Particle.BLOCK_MARKER), Offsettable<BlockMarker>, UsesBlockData<BlockMarker>
        class Bubble : Single<Bubble>(Particle.BUBBLE), Directional<Bubble>
        class BubbleColumnUp : Single<BubbleColumnUp>(Particle.BUBBLE_COLUMN_UP), Directional<BubbleColumnUp>
        class BubblePop : Single<BubblePop>(Particle.BUBBLE_POP), Directional<BubblePop>
        class CampfireCosySmoke : Single<CampfireCosySmoke>(Particle.CAMPFIRE_COSY_SMOKE), Directional<CampfireCosySmoke>
        class CampfireSignalSmoke : Single<CampfireSignalSmoke>(Particle.CAMPFIRE_SIGNAL_SMOKE), Directional<CampfireSignalSmoke>
        class CherryLeaves : Single<CherryLeaves>(Particle.CHERRY_LEAVES), Offsettable<CherryLeaves>
        class Cloud : Single<Cloud>(Particle.CLOUD), Directional<Cloud>
        class Composter : Single<Composter>(Particle.COMPOSTER), Offsettable<Composter>
        class CrimsonSpore : Single<CrimsonSpore>(Particle.CRIMSON_SPORE), Offsettable<CrimsonSpore>
        class Crit : Single<Crit>(Particle.CRIT), Directional<Crit>
        class CurrentDown : Single<CurrentDown>(Particle.CURRENT_DOWN), Offsettable<CurrentDown>
        class DamageIndicator : Single<DamageIndicator>(Particle.DAMAGE_INDICATOR), Directional<DamageIndicator>
        class Dolphin : Single<Dolphin>(Particle.DOLPHIN), Offsettable<Dolphin>
        class DragonBreath : Single<DragonBreath>(Particle.DRAGON_BREATH), Directional<DragonBreath> {
            fun power(power: Float) = apply { innerBuilder.data(power) }
        }
        class DrippingDripstoneLava : Single<DrippingDripstoneLava>(Particle.DRIPPING_DRIPSTONE_LAVA), Offsettable<DrippingDripstoneLava>
        class DrippingDripstoneWater : Single<DrippingDripstoneWater>(Particle.DRIPPING_DRIPSTONE_WATER), Offsettable<DrippingDripstoneWater>
        class DrippingHoney : Single<DrippingHoney>(Particle.DRIPPING_HONEY), Offsettable<DrippingHoney>
        class DrippingLava : Single<DrippingLava>(Particle.DRIPPING_LAVA), Offsettable<DrippingLava>
        class DrippingObsidianTear : Single<DrippingObsidianTear>(Particle.DRIPPING_OBSIDIAN_TEAR), Offsettable<DrippingObsidianTear>
        class DrippingWater : Single<DrippingWater>(Particle.DRIPPING_WATER), Offsettable<DrippingWater>
        class Dust : Single<Dust>(Particle.DUST), Directional<Dust> {
            fun options(options: Particle.DustOptions) = apply { innerBuilder.data(options) }
            fun options(color: Color, size: Float) = options(Particle.DustOptions(color, size))
        }
        class DustColorTransition : Single<DustColorTransition>(Particle.DUST_COLOR_TRANSITION), Directional<DustColorTransition> {
            fun transition(transition: Particle.DustTransition) = apply { innerBuilder.data(transition) }
            fun transition(from: Color, to: Color, size: Float) = transition(Particle.DustTransition(from, to, size))
        }
        class DustPillar : Single<DustPillar>(Particle.DUST_PILLAR), UsesBlockData<DustPillar> {
            fun speed(speed: Double) = apply { innerBuilder.offset(0.0, speed, 0.0) }
        }
        class DustPlume : Single<DustPlume>(Particle.DUST_PLUME), Directional<DustPlume>
        class Effect : Single<Effect>(Particle.EFFECT), Offsettable<Effect>, Rising<Effect>
        class EggCrack : Single<EggCrack>(Particle.EGG_CRACK), Offsettable<EggCrack>
        class ElderGuardian : Single<ElderGuardian>(Particle.ELDER_GUARDIAN), Offsettable<ElderGuardian>
        class ElectricSpark : Single<ElectricSpark>(Particle.ELECTRIC_SPARK), Directional<ElectricSpark>
        class Enchant : Single<Enchant>(Particle.ENCHANT), Converging<Enchant>
        class EnchantedHit : Single<EnchantedHit>(Particle.ENCHANTED_HIT), Directional<EnchantedHit>
        class EndRod : Single<EndRod>(Particle.END_ROD), Directional<EndRod>
        class EntityEffect : Single<EntityEffect>(Particle.ENTITY_EFFECT), Offsettable<EntityEffect>, Colored<EntityEffect>, Rising<EntityEffect>
        class Explosion : Single<Explosion>(Particle.EXPLOSION), Scalable<Explosion>
        class ExplosionEmitter : Single<ExplosionEmitter>(Particle.EXPLOSION_EMITTER), Offsettable<ExplosionEmitter>
        class FallingDripstoneLava : Single<FallingDripstoneLava>(Particle.FALLING_DRIPSTONE_LAVA), Offsettable<FallingDripstoneLava>
        class FallingDripstoneWater : Single<FallingDripstoneWater>(Particle.FALLING_DRIPSTONE_WATER), Offsettable<FallingDripstoneWater>
        class FallingDust : Single<FallingDust>(Particle.FALLING_DUST), Offsettable<FallingDust>, UsesBlockData<FallingDust>
        class FallingHoney : Single<FallingHoney>(Particle.FALLING_HONEY), Offsettable<FallingHoney>
        class FallingLava : Single<FallingLava>(Particle.FALLING_LAVA), Offsettable<FallingLava>
        class FallingNectar : Single<FallingNectar>(Particle.FALLING_NECTAR), Offsettable<FallingNectar>
        class FallingObsidianTear : Single<FallingObsidianTear>(Particle.FALLING_OBSIDIAN_TEAR), Offsettable<FallingObsidianTear>
        class FallingSporeBlossom : Single<FallingSporeBlossom>(Particle.FALLING_SPORE_BLOSSOM), Offsettable<FallingSporeBlossom>
        class FallingWater : Single<FallingWater>(Particle.FALLING_WATER), Offsettable<FallingWater>
        class Firefly : Single<Firefly>(Particle.FIREFLY), Offsettable<Firefly>
        class Firework : Single<Firework>(Particle.FIREWORK), Directional<Firework>
        class Fishing : Single<Fishing>(Particle.FISHING), Directional<Fishing>
        class Flame : Single<Flame>(Particle.FLAME), Directional<Flame>
        class Flash : Single<Flash>(Particle.FLASH), Directional<Flash>, Colored<Flash>
        class Glow : Single<Glow>(Particle.GLOW), Offsettable<Glow>, Rising<Glow>
        class GlowSquidInk : Single<GlowSquidInk>(Particle.GLOW_SQUID_INK), Directional<GlowSquidInk>
        class Gust : Single<Gust>(Particle.GUST), Offsettable<Gust>
        class GustEmitterLarge : Single<GustEmitterLarge>(Particle.GUST_EMITTER_LARGE), Offsettable<GustEmitterLarge>
        class GustEmitterSmall : Single<GustEmitterSmall>(Particle.GUST_EMITTER_SMALL), Offsettable<GustEmitterSmall>
        class HappyVillager : Single<HappyVillager>(Particle.HAPPY_VILLAGER), Offsettable<HappyVillager>
        class Heart : Single<Heart>(Particle.HEART), Offsettable<Heart>
        class Infested : Single<Infested>(Particle.INFESTED), Offsettable<Infested>, Rising<Infested>
        class InstantEffect : Single<InstantEffect>(Particle.INSTANT_EFFECT), Offsettable<InstantEffect>, Rising<InstantEffect>
        class Item : Single<Item>(Particle.ITEM), Directional<Item> {
            fun item(item: ItemStack) = apply { innerBuilder.data(item) }
        }
        class ItemCobweb : Single<ItemCobweb>(Particle.ITEM_COBWEB), Offsettable<ItemCobweb>
        class ItemSlime : Single<ItemSlime>(Particle.ITEM_SLIME), Offsettable<ItemSlime>
        class ItemSnowball : Single<ItemSnowball>(Particle.ITEM_SNOWBALL), Offsettable<ItemSnowball>
        class LandingHoney : Single<LandingHoney>(Particle.LANDING_HONEY), Offsettable<LandingHoney>
        class LandingLava : Single<LandingLava>(Particle.LANDING_LAVA), Offsettable<LandingLava>
        class LandingObsidianTear : Single<LandingObsidianTear>(Particle.LANDING_OBSIDIAN_TEAR), Offsettable<LandingObsidianTear>
        class LargeSmoke : Single<LargeSmoke>(Particle.LARGE_SMOKE), Directional<LargeSmoke>
        class Lava : Single<Lava>(Particle.LAVA), Offsettable<Lava>
        class Nautilus : Single<Nautilus>(Particle.NAUTILUS), Converging<Nautilus>
        class Note : Single<Note>(Particle.NOTE) {
            // this is so cursed what
            fun color(color: Double) = apply { innerBuilder.offset(color, 0.0, 0.0) }
        }
        class Mycelium : Single<Mycelium>(Particle.MYCELIUM), Offsettable<Mycelium>
        class OminousSpawning : Single<OminousSpawning>(Particle.OMINOUS_SPAWNING), Converging<OminousSpawning>
        class PaleOakLeaves : Single<PaleOakLeaves>(Particle.PALE_OAK_LEAVES), Offsettable<PaleOakLeaves>
        class Poof : Single<Poof>(Particle.POOF), Directional<Poof>
        class Portal : Single<Portal>(Particle.PORTAL), Converging<Portal>
        class RaidOmen : Single<RaidOmen>(Particle.RAID_OMEN), Offsettable<RaidOmen>, Rising<RaidOmen>
        class Rain : Single<Rain>(Particle.RAIN), Offsettable<Rain>
        class ReversePortal : Single<ReversePortal>(Particle.REVERSE_PORTAL), Directional<ReversePortal>
        class Scrape : Single<Scrape>(Particle.SCRAPE), Directional<Scrape>
        class SculkCharge : Single<SculkCharge>(Particle.SCULK_CHARGE), Directional<SculkCharge> {
            fun angle(angle: Float) = apply { innerBuilder.data(angle) }
        }
        class SculkChargePop : Single<SculkChargePop>(Particle.SCULK_CHARGE_POP), Directional<SculkChargePop>
        class SculkSoul : Single<SculkSoul>(Particle.SCULK_SOUL), Directional<SculkSoul>
        class Shriek : Single<Shriek>(Particle.SHRIEK), Offsettable<Shriek> {
            fun delayTicks(ticks: Int) = apply { innerBuilder.data(ticks) }
        }
        class SmallFlame : Single<SmallFlame>(Particle.SMALL_FLAME), Directional<SmallFlame>
        class SmallGust : Single<SmallGust>(Particle.SMALL_GUST), Offsettable<SmallGust>
        class Smoke : Single<Smoke>(Particle.SMOKE), Directional<Smoke>
        class Sneeze : Single<Sneeze>(Particle.SNEEZE), Directional<Sneeze>
        class Snowflake : Single<Snowflake>(Particle.SNOWFLAKE), Directional<Snowflake>
        class SonicBoom : Single<SonicBoom>(Particle.SONIC_BOOM), Offsettable<SonicBoom>
        class Soul : Single<Soul>(Particle.SOUL), Directional<Soul>
        class SoulFireFlame : Single<SoulFireFlame>(Particle.SOUL_FIRE_FLAME), Directional<SoulFireFlame>
        class Spit : Single<Spit>(Particle.SPIT), Directional<Spit>
        class Splash : Single<Splash>(Particle.SPLASH), Offsettable<Splash>
        class SporeBlossomAir : Single<SporeBlossomAir>(Particle.SPORE_BLOSSOM_AIR), Offsettable<SporeBlossomAir>
        class SquidInk : Single<SquidInk>(Particle.SQUID_INK), Directional<SquidInk>
        class SweepAttack : Single<SweepAttack>(Particle.SWEEP_ATTACK), Scalable<SweepAttack>
        class TintedLeaves : Single<TintedLeaves>(Particle.TINTED_LEAVES), Offsettable<TintedLeaves>, Colored<TintedLeaves>
        class TotemOfUndying : Single<TotemOfUndying>(Particle.TOTEM_OF_UNDYING), Directional<TotemOfUndying>
        class Trail : Single<Trail>(Particle.TRAIL), Offsettable<Trail> {
            fun trail(trail: Particle.Trail) = apply { innerBuilder.data(trail) }
            fun trail(target: Location, color: Color, duration: Int) = trail(Particle.Trail(target, color, duration))
        }
        class TrialOmen : Single<TrialOmen>(Particle.TRIAL_OMEN), Offsettable<TrialOmen>, Rising<TrialOmen>
        class TrialSpawnerDetection : Single<TrialSpawnerDetection>(Particle.TRIAL_SPAWNER_DETECTION), Directional<TrialSpawnerDetection>
        class TrialSpawnerDetectionOminous : Single<TrialSpawnerDetectionOminous>(Particle.TRIAL_SPAWNER_DETECTION_OMINOUS), Directional<TrialSpawnerDetectionOminous>
        class Underwater : Single<Underwater>(Particle.UNDERWATER), Offsettable<Underwater>
        class VaultConnection : Single<VaultConnection>(Particle.VAULT_CONNECTION), Converging<VaultConnection>
        class Vibration : Single<Vibration>(Particle.VIBRATION) {
            fun vibration(vibration: org.bukkit.Vibration) = apply { innerBuilder.data(vibration) }
        }
        class WarpedSpore : Single<WarpedSpore>(Particle.WARPED_SPORE), Offsettable<WarpedSpore>
        class WaxOff : Single<WaxOff>(Particle.WAX_OFF), Directional<WaxOff>
        class WaxOn : Single<WaxOn>(Particle.WAX_ON), Directional<WaxOn>
        class WhiteAsh : Single<WhiteAsh>(Particle.WHITE_ASH), Offsettable<WhiteAsh>
        class WhiteSmoke : Single<WhiteSmoke>(Particle.WHITE_SMOKE), Directional<WhiteSmoke>
        class Witch : Single<Witch>(Particle.WITCH), Offsettable<Witch>, Rising<Witch>
    }
    // @formatter:on
}