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
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.pylonKey
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import net.kyori.adventure.text.Component
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
import kotlin.time.Duration.Companion.seconds

data class Research(
    private val key: NamespacedKey,
    val name: String,
    val cost: Int,
    val unlocks: Set<NamespacedKey>
) : Keyed {

    constructor(
        key: NamespacedKey,
        name: String,
        cost: Int,
        vararg unlocks: PylonItemSchema
    ) : this(key, name, cost, unlocks.map(PylonItemSchema::getKey).toSet())

    override fun getKey() = key

    companion object : Listener {
        private val researchesKey = pylonKey("researches")
        private val researchesType = PylonSerializers.SET.setTypeFrom(PylonSerializers.NAMESPACED_KEY)

        @JvmStatic
        var Player.researches: Set<NamespacedKey>
            get() = persistentDataContainer.getOrDefault(researchesKey, researchesType, emptySet())
            set(value) = persistentDataContainer.set(researchesKey, researchesType, value)

        @JvmStatic
        fun Player.addResearch(research: NamespacedKey) {
            this.researches = this.researches + research
        }

        @JvmStatic
        fun Player.removeResearch(research: NamespacedKey) {
            this.researches = this.researches - research
        }

        @JvmStatic
        fun Player.hasResearch(research: NamespacedKey): Boolean {
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
                !PylonConfig.researchesEnabled
                || gameMode == GameMode.CREATIVE
                || hasPermission(item.permission)
            ) return true
            val research = item.research
            val result = research != null && hasResearch(research.key)

            if (!result && sendMessage) {
                sendMessage(
                    Component.translatable(
                        "pylon.pyloncore.message.no_item_research",
                        PylonArgument.of("item", item.itemStack.effectiveName())
                    )
                )
            }

            return result
        }

        @JvmStatic
        fun Player.clearResearches() {
            this.researches = emptySet()
        }

        private val playerCheckerJobs = mutableMapOf<UUID, Job>()

        @EventHandler(priority = EventPriority.MONITOR)
        private fun onPlayerJoin(event: PlayerJoinEvent) {
            if (PylonConfig.researchesEnabled) {
                val player = event.player
                // This task runs just in case a player manages to obtain an
                // unknown item without picking it up somehow
                playerCheckerJobs[player.uniqueId] = pluginInstance.launch {
                    player.ejectUnknownItems()
                    delay(5.seconds)
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

        @EventHandler
        private fun onPlayerLeave(event: PlayerQuitEvent) {
            playerCheckerJobs[event.player.uniqueId]?.cancel()
        }
    }
}

private fun Player.ejectUnknownItems() {
    inventory.removeAll { item ->
        val pylonItem = PylonItem.fromStack(item)?.schema ?: return@removeAll false
        if (!canUse(pylonItem, sendMessage = true)) {
            world.dropItem(location, item)
            true
        } else {
            false
        }
    }
}
