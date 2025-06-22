@file:JvmSynthetic // hide the extension functions

package io.github.pylonmc.pylon.core.item.research

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.config.PylonConfig
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.event.PrePylonCraftEvent
import io.github.pylonmc.pylon.core.i18n.PylonArgument
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.item.research.Research.Companion.canUse
import io.github.pylonmc.pylon.core.recipe.RecipeType
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.persistentData
import io.github.pylonmc.pylon.core.util.pylonKey
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.Recipe
import java.util.UUID

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
    fun addTo(player: Player, sendMessage: Boolean = true) {
        if (this in player.researches) return

        player.researches += this
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

        @JvmStatic
        var Player.researchPoints: Long by persistentData(researchPointsKey, PylonSerializers.LONG, 0)

        @JvmStatic
        var Player.researches: Set<Research> by persistentData(researchesKey, researchesType, emptySet())

        @JvmStatic
        @get:JvmName("getResearchFor")
        val PylonItem.research: Research?
            get() = PylonRegistry.RESEARCHES.find { this.key in it.unlocks }

        @JvmStatic
        @JvmOverloads
        @JvmName("canPlayerUse")
        fun Player.canUse(item: PylonItem, sendMessage: Boolean = false): Boolean {
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

            val canCraft = event.recipe.getOutputItems().all {
                val item = PylonItem.fromStack(it)
                if (item == null) {
                    true
                } else {
                    event.player.canUse(item)
                }
            }

            if (!canCraft) {
                event.isCancelled = true
            }
        }
    }
}

fun Player.ejectUnknownItems() {
    val toRemove = inventory.contents.filterNotNull().filter { item ->
        val pylonItem = PylonItem.fromStack(item)
        pylonItem != null && !canUse(pylonItem, sendMessage = true)
    }
    for (item in toRemove) {
        inventory.remove(item)
        dropItem(item)
    }
}

fun Player.addResearch(research: Research, sendMessage: Boolean = false) {
    research.addTo(this, sendMessage)
}

fun Player.removeResearch(research: Research) {
    research.removeFrom(this)
}

fun Player.hasResearch(research: Research): Boolean {
    return research.isResearchedBy(this)
}
