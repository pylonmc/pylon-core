package io.github.pylonmc.pylon.core.registry

import io.github.pylonmc.pylon.core.addon.PylonAddon
import io.github.pylonmc.pylon.core.block.PylonBlockSchema
import io.github.pylonmc.pylon.core.test.GameTestConfig
import io.github.pylonmc.pylon.core.util.pylonKey

object PylonRegistries {

    @JvmField
    val BLOCKS = PylonRegistry<PylonBlockSchema>(pylonKey("blocks"))

    @JvmField
    val ADDONS = PylonRegistry<PylonAddon>(pylonKey("addons"))

    @JvmField
    val GAMETESTS = PylonRegistry<GameTestConfig>(pylonKey("gametests"))
}