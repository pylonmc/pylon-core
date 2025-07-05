package io.github.pylonmc.pylon.core.entity

import io.github.pylonmc.pylon.core.util.findConstructorMatching
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import java.lang.invoke.MethodHandle


class PylonEntitySchema(
    private val key: NamespacedKey,
    val entityClass: Class<*>,
    pylonEntityClass: Class<out PylonEntity<*>>,
) : Keyed {

    @JvmSynthetic
    internal val loadConstructor: MethodHandle = pylonEntityClass.findConstructorMatching(entityClass)
        ?: throw NoSuchMethodException("Entity '$key' (${pylonEntityClass.simpleName}) is missing a load constructor (${entityClass.simpleName})")

    override fun getKey(): NamespacedKey = key

    override fun equals(other: Any?): Boolean = key == (other as? PylonEntitySchema)?.key

    override fun hashCode(): Int = key.hashCode()
}