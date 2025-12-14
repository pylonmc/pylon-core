package io.github.pylonmc.pylon.core.datatypes

import io.github.pylonmc.pylon.core.block.base.PylonProcessor
import io.github.pylonmc.pylon.core.block.base.PylonRecipeProcessor
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.pylonKey
import io.github.pylonmc.pylon.core.util.setNullable
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

object ProcessorDataPersistentDataType : PersistentDataType<PersistentDataContainer, PylonProcessor.ProcessorData> {

    private val PROCESS_TIME_TICKS_KEY = pylonKey("total_process_ticks")
    private val PROCESS_TICKS_REMAINING_KEY = pylonKey("process_ticks_remaining")

    override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java

    override fun getComplexType(): Class<PylonProcessor.ProcessorData> = PylonProcessor.ProcessorData::class.java

    override fun fromPrimitive(primitive: PersistentDataContainer, context: PersistentDataAdapterContext): PylonProcessor.ProcessorData {
        return PylonProcessor.ProcessorData(
            primitive.get(PROCESS_TIME_TICKS_KEY, PylonSerializers.INTEGER),
            primitive.get(PROCESS_TICKS_REMAINING_KEY, PylonSerializers.INTEGER),
            null
        )
    }

    override fun toPrimitive(complex: PylonProcessor.ProcessorData, context: PersistentDataAdapterContext): PersistentDataContainer {
        val pdc = context.newPersistentDataContainer()
        pdc.setNullable(PROCESS_TIME_TICKS_KEY, PylonSerializers.INTEGER, complex.processTimeTicks)
        pdc.setNullable(PROCESS_TICKS_REMAINING_KEY, PylonSerializers.INTEGER, complex.processTicksRemaining)
        return pdc
    }
}
