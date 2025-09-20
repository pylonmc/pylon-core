package io.github.pylonmc.pylon.core.item.research

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.config.PylonConfig
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.event.PrePylonCraftEvent
import io.github.pylonmc.pylon.core.i18n.PylonArgument
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.item.research.Research.Companion.canPickUp
import io.github.pylonmc.pylon.core.particles.ConfettiParticle
import io.github.pylonmc.pylon.core.recipe.FluidOrItem
import io.github.pylonmc.pylon.core.recipe.RecipeType
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.persistentData
import io.github.pylonmc.pylon.core.util.pylonKey
import kotlinx.coroutines.delay
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import org.bukkit.Bukkit
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent

/**
 * @property cost If null, the research cannot be unlocked using points
 * @property unlocks the keys of the items that are unlocked by this research
 */
data class Research(
    private val key: NamespacedKey,
    val material: Material,
    val name: Component,
    val cost: Long?,
    val unlocks: Set<NamespacedKey>
) : Keyed {

    constructor(key: NamespacedKey, material: Material, cost: Long?, vararg unlocks: NamespacedKey) : this(
        key,
        material,
        Component.translatable("pylon.${key.namespace}.research.${key.key}"),
        cost,
        unlocks.toSet()
    )

    fun register() {
        PylonRegistry.RESEARCHES.register(this)
    }

    @JvmOverloads
    fun addTo(player: Player, effects: Boolean = true) {
        if (this in player.researches) return

        player.researches += this
        for (recipe in RecipeType.vanillaCraftingRecipes()) {
            val pylonItem = PylonItem.fromStack(recipe.craftingRecipe.result) ?: continue
            if (pylonItem.key in unlocks) {
                player.discoverRecipe(recipe.key)
            }
        }

        if (effects) {
            val multiplier = ((cost?.toDouble() ?: 0.0) / 5.0)
            val amount = (PylonConfig.researchBaseConfettiAmount * multiplier).toInt()
            ConfettiParticle.Factory.many(player.location, amount).run()

            fun Sound.playSoundLater(delay: Long, pitch: Float = 1f) {
                Bukkit.getScheduler().runTaskLater(PylonCore, Runnable {
                    player.playSound(player.location, this, 1.5f, pitch)
                }, delay)
            }

            repeat(2) {
                Sound.ENTITY_FIREWORK_ROCKET_BLAST.playSoundLater(3L * it)
                Sound.ENTITY_PLAYER_LEVELUP.playSoundLater(6L * it, 0.9f)
                Sound.ENTITY_FIREWORK_ROCKET_LAUNCH.playSoundLater(9L * it)
            }

            player.sendMessage(
                Component.translatable(
                    "pylon.pyloncore.message.research.unlocked",
                    PylonArgument.of("research", name)
                )
            )
        }
    }

    fun removeFrom(player: Player) {
        if (this !in player.researches) return

        player.researches -= this
        for (recipe in RecipeType.vanillaCraftingRecipes()) {
            val pylonItem = PylonItem.fromStack(recipe.craftingRecipe.result) ?: continue
            if (pylonItem.key in unlocks) {
                player.undiscoverRecipe(recipe.key)
            }
        }
    }

    fun isResearchedBy(player: Player): Boolean {
        return this in player.researches
    }

    override fun getKey() = key

    override fun equals(other: Any?): Boolean = other is Research && this.key == other.key

    override fun hashCode(): Int = key.hashCode()

    companion object : Listener {
        private val researchesKey = pylonKey("researches")
        private val researchPointsKey = pylonKey("research_points")
        private val researchesType = PylonSerializers.SET.setTypeFrom(PylonSerializers.KEYED.keyedTypeFrom(PylonRegistry.RESEARCHES::getOrThrow))

        @get:JvmStatic
        @set:JvmStatic
        var Player.researchPoints: Long by persistentData(researchPointsKey, PylonSerializers.LONG, 0)

        @get:JvmStatic
        @set:JvmStatic
        var Player.researches: Set<Research> by persistentData(researchesKey, researchesType, mutableSetOf())

        @get:JvmStatic
        @get:JvmName("getResearchFor")
        val PylonItem.research: Research?
            /**
             * Returns the research that unlocks this item, or null if no such research exists
             */
            get() = PylonRegistry.RESEARCHES.find { this.key in it.unlocks }

        @JvmStatic
        @JvmOverloads
        @JvmName("canPlayerCraft")
        fun Player.canCraft(item: PylonItem, sendMessage: Boolean = false): Boolean {
            if (!PylonConfig.researchesEnabled || this.hasPermission(item.researchBypassPermission)) return true

            val research = item.research ?: return true

            val canUse = this.hasResearch(research)
            if (!canUse && sendMessage) {
                var researchName = research.name
                if (research.cost != null) {
                    researchName = researchName
                        .hoverEvent(
                            HoverEvent.showText(
                                Component.translatable(
                                    "pylon.pyloncore.message.research.click_to_research",
                                    PylonArgument.of("points", research.cost)
                                )
                            )
                        )
                        .clickEvent(ClickEvent.runCommand("/pylon research discover ${research.key}"))
                }
                this.sendMessage(
                    Component.translatable(
                        "pylon.pyloncore.message.research.unknown",
                        PylonArgument.of("item", item.stack.effectiveName()),
                        PylonArgument.of("research", researchName)
                    )
                )
            }

            return canUse
        }

        @JvmStatic
        @JvmOverloads
        @JvmName("canPlayerPickUp")
        fun Player.canPickUp(item: PylonItem, sendMessage: Boolean = false): Boolean
            = canCraft(item, sendMessage)

        @JvmStatic
        @JvmOverloads
        @JvmName("canPlayerUse")
        fun Player.canUse(item: PylonItem, sendMessage: Boolean = false): Boolean {
            if (PylonConfig.disabledItems.contains(item.key)) {
                if (sendMessage) {
                    this.sendMessage(
                        Component.translatable(
                            "pylon.pyloncore.message.disabled.message",
                            PylonArgument.of("item", item.stack.effectiveName()),
                        )
                    )
                }
                return false
            }

            return canCraft(item, sendMessage)
        }

        @JvmStatic
        fun Player.clearResearches() {
            this.researches = emptySet()
        }

        @EventHandler
        private fun onPlayerPickup(event: EntityPickupItemEvent) {
            val entity = event.entity
            if (entity is Player) {
                PylonCore.launch {
                    delay(1.ticks)
                    entity.ejectUnknownItems()
                }
            }
        }

        @EventHandler
        private fun onPrePylonCraft(event: PrePylonCraftEvent<*>) {
            if (event.player == null) {
                return
            }

            val canCraft = event.recipe.results.all {
                when (it) {
                    is FluidOrItem.Item ->  {
                        val item = PylonItem.fromStack(it.item)
                        item == null || event.player.canCraft(item, true)
                    }
                    else -> true
                }
            }

            if (!canCraft) {
                event.isCancelled = true
            }
        }
    }
}

@JvmSynthetic
private fun Player.ejectUnknownItems() {
    val toRemove = inventory.contents.filterNotNull().filter { item ->
        val pylonItem = PylonItem.fromStack(item)
        pylonItem != null && !canPickUp(pylonItem, sendMessage = true)
    }
    for (item in toRemove) {
        inventory.remove(item)
        dropItem(item)
    }
}

@JvmSynthetic
fun Player.addResearch(research: Research, sendMessage: Boolean = false) {
    research.addTo(this, sendMessage)
}

@JvmSynthetic
fun Player.removeResearch(research: Research) {
    research.removeFrom(this)
}

@JvmSynthetic
fun Player.hasResearch(research: Research): Boolean {
    return research.isResearchedBy(this)
}
