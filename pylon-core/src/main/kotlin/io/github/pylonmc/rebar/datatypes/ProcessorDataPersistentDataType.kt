package io.github.pylonmc.rebar.datatypes

import io.github.pylonmc.rebar.block.base.PylonProcessor
import io.github.pylonmc.rebar.util.rebarKey
import io.github.pylonmc.rebar.util.setNullable
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

internal object ProcessorDataPersistentDataType : PersistentDataType<PersistentDataContainer, PylonProcessor.ProcessorData> {

    private val PROCESS_TIME_TICKS_KEY = rebarKey("total_process_ticks")
    private val PROCESS_TICKS_REMAINING_KEY = rebarKey("process_ticks_remaining")
    private val PROGRESS_ITEM_KEY = rebarKey("progress_item")

    override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java

    override fun getComplexType(): Class<PylonProcessor.ProcessorData> = PylonProcessor.ProcessorData::class.java

    override fun fromPrimitive(primitive: PersistentDataContainer, context: PersistentDataAdapterContext): PylonProcessor.ProcessorData {
        return PylonProcessor.ProcessorData(
            primitive.get(PROCESS_TIME_TICKS_KEY, PylonSerializers.INTEGER),
            primitive.get(PROCESS_TICKS_REMAINING_KEY, PylonSerializers.INTEGER),
            primitive.get(PROGRESS_ITEM_KEY, PylonSerializers.PROGRESS_ITEM),
        )
    }

    override fun toPrimitive(complex: PylonProcessor.ProcessorData, context: PersistentDataAdapterContext): PersistentDataContainer {
        val pdc = context.newPersistentDataContainer()
        pdc.setNullable(PROCESS_TIME_TICKS_KEY, PylonSerializers.INTEGER, complex.processTimeTicks)
        pdc.setNullable(PROCESS_TICKS_REMAINING_KEY, PylonSerializers.INTEGER, complex.processTicksRemaining)
        pdc.setNullable(PROGRESS_ITEM_KEY, PylonSerializers.PROGRESS_ITEM, complex.progressItem)
        return pdc
    }
}
