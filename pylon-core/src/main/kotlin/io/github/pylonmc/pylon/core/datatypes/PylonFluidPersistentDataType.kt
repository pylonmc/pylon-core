package io.github.pylonmc.pylon.core.datatypes

import io.github.pylonmc.pylon.core.fluid.PylonFluid
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import org.bukkit.NamespacedKey

object PylonFluidPersistentDataType : KeyedPersistentDataType<PylonFluid>(PylonFluid::class.java) {
    override fun retrieve(key: NamespacedKey): PylonFluid {
        return PylonRegistry.FLUIDS[key] ?: throw IllegalStateException("No such fluid $key")
    }
}