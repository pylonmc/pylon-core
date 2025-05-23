package io.github.pylonmc.pylon.core.entity

import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.findConstructorMatching
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import java.lang.invoke.MethodHandle


open class PylonEntitySchema(
    private val key: NamespacedKey,
    val entityClass: Class<*>,
    pylonEntityClass: Class<out PylonEntity<*, *>>,
) : Keyed {

    @JvmSynthetic
    internal val loadConstructor: MethodHandle = pylonEntityClass.findConstructorMatching(
        javaClass,
        entityClass
    ) ?: throw NoSuchMethodException(
        "Entity '$key' ($pylonEntityClass) is missing a load constructor (${javaClass.simpleName}, ${entityClass.simpleName})"
    )

    fun register() {
        PylonRegistry.ENTITIES.register(this)
    }

    override fun getKey(): NamespacedKey = key
}