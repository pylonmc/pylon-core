package io.github.pylonmc.pylon.core.item.research

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.config.ConfigSection
import io.github.pylonmc.pylon.core.config.PylonConfig
import io.github.pylonmc.pylon.core.config.adapter.ConfigAdapter
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.i18n.PylonArgument
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.item.research.Research.Companion.canPickUp
import io.github.pylonmc.pylon.core.particles.ConfettiParticle
import io.github.pylonmc.pylon.core.recipe.FluidOrItem
import io.github.pylonmc.pylon.core.recipe.RecipeType
import io.github.pylonmc.pylon.core.recipe.vanilla.VanillaRecipeType
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.persistentData
import io.github.pylonmc.pylon.core.util.pylonKey
import kotlinx.coroutines.delay
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import org.bukkit.Bukkit
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.player.PlayerJoinEvent
import kotlin.math.min

/**
 * A Pylon research as seem in the 'researches' guide section.
 *
 * Researches typically have a research point [cost] specified. However, this
 * is optional, and you can implement your own methods to unlock a research.
 *
 * @property cost If null, the research cannot be unlocked using points
 * @property unlocks The keys of the items that are unlocked by this research
 */
@JvmRecord
data class Research(
    private val key: NamespacedKey,
    val material: Material,
    val name: Component,
    val cost: Long?,
    val unlocks: Set<NamespacedKey>
) : Keyed {

    /**
     * A constructor that sets the name to a default language file key.
     */
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

    /**
     * Adds the research to a player.
     *
     * @param sendMessage If set, sends a message to notify the player that they
     * have unlocked the research
     */
    @JvmOverloads
    fun addTo(player: Player, sendMessage: Boolean = true, effects: Boolean = true) {
        if (this in getResearches(player)) return

        addResearch(player, this)
        for (recipe in RecipeType.vanillaCraftingRecipes()) {
            val pylonItem = PylonItem.fromStack(recipe.craftingRecipe.result) ?: continue
            if (pylonItem.key in unlocks) {
                player.discoverRecipe(recipe.key)
            }
        }

        if (sendMessage) {
            player.sendMessage(
                Component.translatable(
                    "pylon.pyloncore.message.research.unlocked",
                    PylonArgument.of("research", name)
                )
            )
        }

        if (effects) {
            if (player.researchConfetti) {
                val multiplier = (cost?.toDouble() ?: 0.0) * PylonConfig.RESEARCH_MULTIPLIER_CONFETTI_AMOUNT
                val amount = (PylonConfig.RESEARCH_BASE_CONFETTI_AMOUNT * multiplier).toInt()
                val spawnedConfetti = min(amount, PylonConfig.RESEARCH_MAX_CONFETTI_AMOUNT)
                ConfettiParticle.spawnMany(player.location, spawnedConfetti).run()
            }

            if (player.researchSounds) {
                for ((delay, sound) in PylonConfig.RESEARCH_SOUNDS) {
                    Bukkit.getScheduler().runTaskLater(PylonCore, Runnable {
                        player.playSound(sound.create(), Sound.Emitter.self())
                    }, delay)
                }
            }
        }
    }

    /**
     * Removes a research from a player.
     */
    fun removeFrom(player: Player) {
        if (this !in getResearches(player)) return

        removeResearch(player, this)
        for (recipe in RecipeType.vanillaCraftingRecipes()) {
            val pylonItem = PylonItem.fromStack(recipe.craftingRecipe.result) ?: continue
            if (pylonItem.key in unlocks) {
                player.undiscoverRecipe(recipe.key)
            }
        }
    }

    /**
     * Returns whether a research has been researched by the given player.
     */
    fun isResearchedBy(player: Player): Boolean {
        return this in getResearches(player)
    }

    override fun getKey() = key

    override fun equals(other: Any?): Boolean = other is Research && this.key == other.key

    override fun hashCode(): Int = key.hashCode()

    companion object : Listener {
        private val researchesKey = pylonKey("researches")
        private val researchPointsKey = pylonKey("research_points")
        private val researchEffectsKey = pylonKey("research_effects")
        private val researchesType =
            PylonSerializers.SET.setTypeFrom(PylonSerializers.KEYED.keyedTypeFrom(PylonRegistry.RESEARCHES::getOrThrow))

        @JvmStatic
        var Player.researchPoints: Long by persistentData(researchPointsKey, PylonSerializers.LONG, 0)

        @JvmStatic
        var Player.researchConfetti: Boolean by persistentData(researchEffectsKey, PylonSerializers.BOOLEAN, true)

        @JvmStatic
        var Player.researchSounds: Boolean by persistentData(researchEffectsKey, PylonSerializers.BOOLEAN, true)

        @JvmStatic
        fun getResearches(player: OfflinePlayer): Set<Research> {
            val researches = player.persistentDataContainer.get(researchesKey, researchesType)
            if (researches == null && player is Player) {
                setResearches(player, setOf())
                return setOf()
            }
            return researches!!
        }

        @JvmStatic
        fun setResearches(player: Player, researches: Set<Research>)
            = player.persistentDataContainer.set(researchesKey, researchesType, researches)

        @JvmStatic
        fun addResearch(player: Player, research: Research)
            = setResearches(player, getResearches(player) + research)

        @JvmStatic
        fun removeResearch(player: Player, research: Research)
            = setResearches(player, getResearches(player) - research)

        /**
         * Checks whether a player can craft an item (ie has the associated research, or
         * has permission to bypass research.
         *
         * @param sendMessage Whether, if the player cannot craft the item, a message should be sent to them
         * to notify them of this fact
         */
        @JvmStatic
        @JvmOverloads
        @JvmName("canPlayerCraft")
        fun Player.canCraft(item: PylonItem, sendMessage: Boolean = false): Boolean {
            if (!PylonConfig.RESEARCHES_ENABLED || this.hasPermission(item.researchBypassPermission)) return true

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

        /**
         * Checks whether a player can pick up an item (ie has the associated research, or
         * has permission to bypass research.
         *
         * @param sendMessage Whether, if the player cannot pick up the item, a message should be sent to them
         * to notify them of this fact
         */
        @JvmStatic
        @JvmOverloads
        @JvmName("canPlayerPickUp")
        fun Player.canPickUp(item: PylonItem, sendMessage: Boolean = false): Boolean = canCraft(item, sendMessage)

        /**
         * Checks whether a player can use an item (ie has the associated research, or
         * has permission to bypass research.
         *
         * @param sendMessage Whether, if the player cannot use the item, a message should be sent to them
         * to notify them of this fact
         */
        @JvmStatic
        @JvmOverloads
        @JvmName("canPlayerUse")
        fun Player.canUse(item: PylonItem, sendMessage: Boolean = false): Boolean {
            if (PylonConfig.DISABLED_ITEMS.contains(item.key)) {
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
        private fun onJoin(e: PlayerJoinEvent) {
            if (!PylonConfig.RESEARCHES_ENABLED) return
            val player = e.player

            // discover only the recipes that have no research whenever an ingredient is added to the inventory
            for (recipeType in PylonRegistry.RECIPE_TYPES) {
                if (recipeType !is VanillaRecipeType<*>) continue
                for (recipe in recipeType) {
                    if (recipe.key in VanillaRecipeType.nonPylonRecipes) continue
                    val researches = recipe.results
                        .filterIsInstance<FluidOrItem.Item>()
                        .mapNotNull { PylonItem.fromStack(it.item)?.research }
                    if (researches.isNotEmpty()) continue
                    player.discoverRecipe(recipe.key)
                }
            }
        }


        @JvmStatic
        fun loadFromConfig(section: ConfigSection, key : NamespacedKey) : Research {

            try {
                val material = section.getOrThrow("material", ConfigAdapter.MATERIAL)
                val name = section.get("name", ConfigAdapter.STRING) ?: "pylon.${key.namespace}.research.${key.key}"
                val cost = section.get("cost", ConfigAdapter.LONG)
                val unlocks = section.get("unlocks", ConfigAdapter.SET.from(ConfigAdapter.NAMESPACED_KEY)) ?: emptySet()

                return Research(key, material, Component.translatable(name), cost, unlocks)
            } catch (e: Exception) {
                throw IllegalArgumentException(
                    "Failed to load research '$key' from config",
                    e
                )
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
fun Player.addResearch(research: Research, sendMessage: Boolean = false, confetti: Boolean = true) {
    research.addTo(this, sendMessage, confetti)
}

@JvmSynthetic
fun Player.removeResearch(research: Research) {
    research.removeFrom(this)
}

@JvmSynthetic
fun Player.hasResearch(research: Research): Boolean {
    return research.isResearchedBy(this)
}
