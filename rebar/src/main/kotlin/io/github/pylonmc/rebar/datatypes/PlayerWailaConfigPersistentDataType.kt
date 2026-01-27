package io.github.pylonmc.rebar.datatypes

import io.github.pylonmc.rebar.util.rebarKey
import io.github.pylonmc.rebar.waila.PlayerWailaConfig
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

object PlayerWailaConfigPersistentDataType : PersistentDataType<PersistentDataContainer, PlayerWailaConfig> {
    val enabledKey = rebarKey("waila_enabled")
    val vanillaWailaEnabledKey = rebarKey("waila_vanilla_enabled")
    val typeKey = rebarKey("waila_type")

    override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java

    override fun getComplexType(): Class<PlayerWailaConfig> = PlayerWailaConfig::class.java

    override fun fromPrimitive(
        primitive: PersistentDataContainer,
        context: PersistentDataAdapterContext
    ): PlayerWailaConfig {
        val enabled = primitive.get(enabledKey, RebarSerializers.BOOLEAN)!!
        val vanillaWailaEnabled = primitive.get(vanillaWailaEnabledKey, RebarSerializers.BOOLEAN)!!
        val type = primitive.get(typeKey, RebarSerializers.WAILA_TYPE)!!
        return PlayerWailaConfig(enabled, vanillaWailaEnabled, type)
    }

    override fun toPrimitive(
        complex: PlayerWailaConfig,
        context: PersistentDataAdapterContext
    ): PersistentDataContainer {
        val pdc = context.newPersistentDataContainer()
        pdc.set(enabledKey, RebarSerializers.BOOLEAN, complex.enabled)
        pdc.set(vanillaWailaEnabledKey, RebarSerializers.BOOLEAN, complex.vanillaWailaEnabled)
        pdc.set(typeKey, RebarSerializers.WAILA_TYPE, complex.type)
        return pdc
    }
}