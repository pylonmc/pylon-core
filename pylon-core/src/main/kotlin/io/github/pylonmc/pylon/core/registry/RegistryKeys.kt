package io.github.pylonmc.pylon.core.registry

import io.github.pylonmc.pylon.core.block.PylonBlockSchema
import io.github.pylonmc.pylon.core.util.pylonKey

object RegistryKeys {

    @JvmField
    val BLOCKS = RegistryKey<PylonBlockSchema>(pylonKey("blocks"))
}