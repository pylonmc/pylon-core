package io.github.pylonmc.rebar.datatypes

import io.github.pylonmc.rebar.util.gui.ProgressItem
import io.github.pylonmc.rebar.util.rebarKey
import io.github.pylonmc.rebar.util.setNullable
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

object ProgressItemPersistentDataType : PersistentDataType<PersistentDataContainer, ProgressItem> {
    val itemStackKey = rebarKey("item_stack")
    val countDownKey = rebarKey("count_down")
    val totalTimeKey = rebarKey("total_time")
    val progressKey = rebarKey("progress")

    override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java

    override fun getComplexType(): Class<ProgressItem> = ProgressItem::class.java

    override fun fromPrimitive(
        primitive: PersistentDataContainer,
        context: PersistentDataAdapterContext
    ): ProgressItem {
        val stack = primitive.get(itemStackKey, RebarSerializers.ITEM_STACK)!!
        val countDown = primitive.get(countDownKey, RebarSerializers.BOOLEAN)!!
        val item = ProgressItem(stack, countDown)
        item.totalTime = primitive.get(totalTimeKey, RebarSerializers.DURATION)
        item.progress = primitive.get(progressKey, RebarSerializers.DOUBLE)!!
        return item
    }

    override fun toPrimitive(complex: ProgressItem, context: PersistentDataAdapterContext): PersistentDataContainer {
        val pdc = context.newPersistentDataContainer()
        pdc.set(itemStackKey, RebarSerializers.ITEM_STACK, complex.itemStackBuilder.stack)
        pdc.set(countDownKey, RebarSerializers.BOOLEAN, complex.countDown)
        pdc.setNullable(totalTimeKey, RebarSerializers.DURATION, complex.totalTime)
        pdc.set(progressKey, RebarSerializers.DOUBLE, complex.progress)
        return pdc
    }
}