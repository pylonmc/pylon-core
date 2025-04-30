package io.github.pylonmc.pylon.core.item.research

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import io.github.pylonmc.pylon.core.config.PylonConfig
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.i18n.PylonArgument
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.item.PylonItemSchema
import io.github.pylonmc.pylon.core.item.research.Research.Companion.canUse
import io.github.pylonmc.pylon.core.pluginInstance
import io.github.pylonmc.pylon.core.recipe.RecipeTypes
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.persistentData
import io.github.pylonmc.pylon.core.util.pylonKey
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import org.bukkit.GameMode
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.UUID

/**
 * @property cost If null, the research cannot be unlocked using points
 * @property unlocks the keys of the items that are unlocked by this research
 */
data class Research(
    private val key: NamespacedKey,
    val name: Component,
    val cost: Long?,
    val unlocks: Set<NamespacedKey>
) : Keyed {

    constructor(key: NamespacedKey, cost: Long?, vararg unlocks: PylonItemSchema) : this(
        key,
        Component.translatable("pylon.${key.namespace}.research.${key.key}"),
        cost,
        unlocks.map(PylonItemSchema::getKey).toSet()
    )

    fun register() {
        PylonRegistry.RESEARCHES.register(this)
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
        @JvmOverloads
        fun Player.addResearch(research: Research, sendMessage: Boolean = false) {
            if (research in this.researches) return

            this.researches += research
            for (recipe in RecipeTypes.VANILLA_CRAFTING) {
                val pylonItem = PylonItem.fromStack(recipe.result)?.schema ?: continue
                if (pylonItem.key in research.unlocks) {
                    discoverRecipe(recipe.key)
                }
            }
            if (sendMessage) {
                this.sendMessage(
                    Component.translatable(
                        "pylon.pyloncore.message.research.unlocked",
                        PylonArgument.of("research", research.name)
                    )
                )
            }
        }

        @JvmStatic
        fun Player.removeResearch(research: Research) {
            this.researches -= research
        }

        @JvmStatic
        fun Player.hasResearch(research: Research): Boolean {
            return research in this.researches
        }

        @JvmStatic
        @get:JvmName("getResearchFor")
        val PylonItemSchema.research: Research?
            get() = PylonRegistry.RESEARCHES.find { this.key in it.unlocks }

        @JvmStatic
        @JvmOverloads
        @JvmName("canPlayerUse")
        fun Player.canUse(item: PylonItemSchema, sendMessage: Boolean = false): Boolean {
            if (
                PylonConfig.researchInterval < 1
                || this.gameMode == GameMode.CREATIVE
                || this.hasPermission(item.researchBypassPermission)
            ) return true

            val research = item.research
            if (research == null) return true

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
                        PylonArgument.of("item", item.itemStack.effectiveName()),
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

        private val playerCheckerJobs = mutableMapOf<UUID, Job>()

        @EventHandler(priority = EventPriority.MONITOR)
        private fun onPlayerJoin(event: PlayerJoinEvent) {
            val interval = PylonConfig.researchInterval
            if (interval > 0) {
                val player = event.player
                // This task runs just in case a player manages to obtain an
                // unknown item without picking it up somehow
                playerCheckerJobs[player.uniqueId] = pluginInstance.launch {
                    while (true) {
                        player.ejectUnknownItems()
                        delay(interval.ticks)
                    }
                }
            }
        }

        @EventHandler
        private fun onPlayerPickup(event: EntityPickupItemEvent) {
            val entity = event.entity
            if (entity is Player) {
                pluginInstance.launch {
                    delay(1.ticks)
                    entity.ejectUnknownItems()
                }
            }
        }

        @EventHandler(priority = EventPriority.MONITOR)
        private fun onPlayerLeave(event: PlayerQuitEvent) {
            playerCheckerJobs[event.player.uniqueId]?.cancel()
        }
    }
}

private fun Player.ejectUnknownItems() {
    val toRemove = inventory.contents.filterNotNull().filter { item ->
        val pylonItem = PylonItem.fromStack(item)?.schema
        pylonItem != null && !canUse(pylonItem, sendMessage = true)
    }
    for (item in toRemove) {
        inventory.remove(item)
        dropItem(item)
    }
}
