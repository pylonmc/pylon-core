package io.github.pylonmc.pylon.core.registry

import io.github.pylonmc.pylon.core.addon.PylonAddon
import io.github.pylonmc.pylon.core.block.PylonBlockSchema
import io.github.pylonmc.pylon.core.util.pylonKey

object PylonRegistryKeys {

    @JvmField
    val BLOCKS = PylonRegistryKey<PylonBlockSchema>(pylonKey("blocks"))
    @JvmField
    val ADDONS = PylonRegistryKey<PylonAddon>(pylonKey("addons"))
}