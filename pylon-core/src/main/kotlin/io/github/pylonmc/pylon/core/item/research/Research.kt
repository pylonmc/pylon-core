package io.github.pylonmc.pylon.core.item.research

import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.item.PylonItemSchema
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player

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

    companion object {
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
        fun Player.clearResearches() {
            this.researches = emptySet()
        }
    }
}
