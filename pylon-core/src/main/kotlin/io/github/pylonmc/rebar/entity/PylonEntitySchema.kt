package io.github.pylonmc.rebar.entity

import io.github.pylonmc.rebar.util.findConstructorMatching
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import java.lang.invoke.MethodHandle

/**
 * Stores information about a Pylon entity type, including its key, vanilla entity class,
 * and Pylion entity class.
 *
 * You should not need to use this if you are not working on Pylon Core.
 */
class PylonEntitySchema(
    private val key: NamespacedKey,
    val entityClass: Class<*>,
    pylonEntityClass: Class<out PylonEntity<*>>,
    isPersistent: Boolean,
) : Keyed {

    @JvmSynthetic
    internal val loadConstructor: MethodHandle? = if (isPersistent) {
        pylonEntityClass.findConstructorMatching(entityClass)
            ?: throw NoSuchMethodException("Entity '$key' (${pylonEntityClass.simpleName}) is missing a load constructor (${entityClass.simpleName})")
    } else {
        null
    }

    override fun getKey(): NamespacedKey = key

    override fun equals(other: Any?): Boolean = key == (other as? PylonEntitySchema)?.key

    override fun hashCode(): Int = key.hashCode()
}