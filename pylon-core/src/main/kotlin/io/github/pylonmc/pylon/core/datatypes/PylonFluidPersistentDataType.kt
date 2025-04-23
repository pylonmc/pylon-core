package io.github.pylonmc.pylon.core.datatypes

import io.github.pylonmc.pylon.core.fluid.PylonFluid
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType

object PylonFluidPersistentDataType : PersistentDataType<String, PylonFluid> {
    override fun getPrimitiveType(): Class<String> = String::class.java

    override fun getComplexType(): Class<PylonFluid> = PylonFluid::class.java

    override fun fromPrimitive(primitive: String, context: PersistentDataAdapterContext): PylonFluid {
        val key = PylonSerializers.NAMESPACED_KEY.fromPrimitive(primitive, context)
        val fluid = PylonRegistry.FLUIDS[key]
        check(fluid != null) { "No such fluid $key" }
        return fluid
    }

    override fun toPrimitive(complex: PylonFluid, context: PersistentDataAdapterContext): String {
        return PylonSerializers.NAMESPACED_KEY.toPrimitive(complex.key, context)
    }
}