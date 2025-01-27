package io.github.pylonmc.pylon.core.registry

import io.github.pylonmc.pylon.core.addon.PylonAddon
import io.github.pylonmc.pylon.core.block.PylonBlockSchema
import io.github.pylonmc.pylon.core.item.PylonItemSchema
import io.github.pylonmc.pylon.core.test.GameTestConfig
import io.github.pylonmc.pylon.core.util.pylonKey
import net.kyori.adventure.key.Key

@JvmRecord
data class PylonRegistryKey<T>(val namespace: String, val path: String) {
    constructor(key: Key) : this(key.namespace(), key.value())

    override fun toString(): String {
        return "$namespace:$path"
    }

    companion object {
        @JvmField
        val ITEMS = PylonRegistryKey<PylonItemSchema>(pylonKey("items"))

        @JvmField
        val BLOCKS = PylonRegistryKey<PylonBlockSchema>(pylonKey("blocks"))

        @JvmField
        val GAMETESTS = PylonRegistryKey<GameTestConfig>(pylonKey("gametests"))

        @JvmField
        val ADDONS = PylonRegistryKey<PylonAddon>(pylonKey("addons"))
    }
}