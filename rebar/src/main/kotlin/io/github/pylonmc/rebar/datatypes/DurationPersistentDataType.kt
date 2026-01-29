package io.github.pylonmc.rebar.datatypes

import io.github.pylonmc.rebar.util.rebarKey
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import java.time.Duration

object DurationPersistentDataType : PersistentDataType<PersistentDataContainer, Duration> {
    val secondsKey = rebarKey("seconds")
    val nanosKey = rebarKey("nanos")

    override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java

    override fun getComplexType(): Class<Duration> = Duration::class.java

    override fun fromPrimitive(
        primitive: PersistentDataContainer,
        context: PersistentDataAdapterContext
    ): Duration {
        val seconds = primitive.get(secondsKey, PersistentDataType.LONG)!!
        val nanos = primitive.get(nanosKey, RebarSerializers.INTEGER)!!
        return Duration.ofSeconds(seconds, nanos.toLong())
    }

    override fun toPrimitive(complex: Duration, context: PersistentDataAdapterContext): PersistentDataContainer {
        val pdc = context.newPersistentDataContainer()
        pdc.set(secondsKey, PersistentDataType.LONG, complex.seconds)
        pdc.set(nanosKey, PersistentDataType.INTEGER, complex.nano)
        return pdc
    }
}