package io.github.pylonmc.pylon.core.registry

import io.github.pylonmc.pylon.core.addon.PylonAddon
import io.github.pylonmc.pylon.core.block.PylonBlockSchema
import io.github.pylonmc.pylon.core.entity.PylonEntitySchema
import io.github.pylonmc.pylon.core.fluid.PylonFluid
import io.github.pylonmc.pylon.core.item.ItemTypeWrapper
import io.github.pylonmc.pylon.core.item.PylonItemSchema
import io.github.pylonmc.pylon.core.item.research.Research
import io.github.pylonmc.pylon.core.mobdrop.MobDrop
import io.github.pylonmc.pylon.core.recipe.RecipeType
import io.github.pylonmc.pylon.core.test.GameTestConfig
import io.github.pylonmc.pylon.core.util.pylonKey
import net.kyori.adventure.key.Key
import org.bukkit.NamespacedKey
import org.bukkit.Tag
import org.bukkit.plugin.Plugin

@JvmRecord
data class PylonRegistryKey<T>(val namespace: String, val path: String) {
    constructor(key: Key) : this(key.namespace(), key.value())
    constructor(plugin: Plugin, path: String) : this(NamespacedKey(plugin, path))

    override fun toString(): String {
        return "$namespace:$path"
    }

    companion object {
        // @formatter:off
        @JvmField val ITEMS = PylonRegistryKey<PylonItemSchema>(pylonKey("items"))
        @JvmField val BLOCKS = PylonRegistryKey<PylonBlockSchema>(pylonKey("blocks"))
        @JvmField val ENTITIES = PylonRegistryKey<PylonEntitySchema>(pylonKey("entities"))
        @JvmField val FLUIDS = PylonRegistryKey<PylonFluid>(pylonKey("fluids"))
        @JvmField val GAMETESTS = PylonRegistryKey<GameTestConfig>(pylonKey("gametests"))
        @JvmField val ADDONS = PylonRegistryKey<PylonAddon>(pylonKey("addons"))
        @JvmField val RECIPE_TYPES = PylonRegistryKey<RecipeType<*>>(pylonKey("recipe_types"))
        @JvmField val MOB_DROPS = PylonRegistryKey<MobDrop>(pylonKey("mob_drops"))
        @JvmField val RESEARCHES = PylonRegistryKey<Research>(pylonKey("researches"))
        @JvmField val ITEM_TAGS = PylonRegistryKey<Tag<ItemTypeWrapper>>(pylonKey("tags"))
        // @formatter:on
    }
}