package io.github.pylonmc.rebar.entity

import io.github.pylonmc.rebar.util.findConstructorMatching
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import java.lang.invoke.MethodHandle

/**
 * Stores information about a Rebar entity type, including its key, vanilla entity class,
 * and Pylion entity class.
 *
 * You should not need to use this if you are not working on Rebar Core.
 */
class RebarEntitySchema(
    private val key: NamespacedKey,
    val entityClass: Class<*>,
    rebarEntityClass: Class<out RebarEntity<*>>,
    isPersistent: Boolean,
) : Keyed {

    @JvmSynthetic
    internal val loadConstructor: MethodHandle? = if (isPersistent) {
        rebarEntityClass.findConstructorMatching(entityClass)
            ?: throw NoSuchMethodException("Entity '$key' (${rebarEntityClass.simpleName}) is missing a load constructor (${entityClass.simpleName})")
    } else {
        null
    }

    override fun getKey(): NamespacedKey = key

    override fun equals(other: Any?): Boolean = key == (other as? RebarEntitySchema)?.key

    override fun hashCode(): Int = key.hashCode()
}