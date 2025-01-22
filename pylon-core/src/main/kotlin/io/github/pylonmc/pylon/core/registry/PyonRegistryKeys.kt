package io.github.pylonmc.pylon.core.registry

import io.github.pylonmc.pylon.core.block.PylonBlockSchema
import io.github.pylonmc.pylon.core.util.pylonKey

object PyonRegistryKeys {

    @JvmField
    val BLOCKS = PylonRegistryKey<PylonBlockSchema>(pylonKey("blocks"))
}