package io.github.pylonmc.pylon.core.entity

import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.config.Config
import io.github.pylonmc.pylon.core.config.Settings
import io.github.pylonmc.pylon.core.content.debug.DebugWaxedWeatheredCutCopperStairs
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.pylonKey
import io.github.pylonmc.pylon.core.waila.WailaDisplay
import org.bukkit.NamespacedKey
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataContainer


/**
 * Represents a Pylon entity in the world.
 *
 * All custom Pylon entities extend this class. Every instance of this class is wrapping a real entity
 * in the world, and is stored in [EntityStorage]. All new block *types* must be registered using [register],
 * and all new Pylon entities must be added to [EntityStorage] with [EntityStorage.add].
 *
 * You are responsible for creating your Pylon entities; there are no place constructors as with
 * Pylon blocks. This is because it doesn't make sense for Pylon to manage spawning entities. However, your
 * entity must still have a load constructor that takes a single parameter of type [E].
 */
abstract class PylonEntity<out E: Entity>(val entity: E) {

    val key = entity.persistentDataContainer.get(pylonEntityKeyKey, PylonSerializers.NAMESPACED_KEY)
        ?: throw IllegalStateException("Entity did not have a Pylon key; did you mean to call PylonEntity(NamespacedKey, Entity) instead of PylonEntity(Entity)?")
    val schema = PylonRegistry.ENTITIES.getOrThrow(key)
    val uuid = entity.uniqueId

    constructor(key: NamespacedKey, entity: E): this(initialisePylonEntity<E>(key, entity))

    /**
     * WAILA is the text that shows up when looking at a block to tell you what the block is. It
     * can also be used for entities.
     *
     * This will only be called for the player if the player has WAILA enabled.
     *
     * @return the WAILA configuration, or null if WAILA should not be shown for this block.
     */
    open fun getWaila(player: Player): WailaDisplay? = null

    /**
     * Called when debug info is requested for the entity by someone
     * using the [DebugWaxedWeatheredCutCopperStairs]. If there is
     * any transient data that can be useful for debugging, you're
     * encouraged to save it here.
     *
     * Defaults to a normal [write] call.
     */
    open fun writeDebugInfo(pdc: PersistentDataContainer) = write(pdc)

    /**
     * Called when the entity is saved.
     *
     * Put any logic to save the data in the entity here.
     *
     * *Do not assume that when this is called, the entity is being unloaded.* This
     * may be called for other reasons, such as when a player right clicks with
     * [DebugWaxedWeatheredCutCopperStairs]. Instead, uise [onUnload]
     */
    open fun write(pdc: PersistentDataContainer) {}

    /**
     * Called when the entity is unloaded, not including when it is deleted.
     */
    open fun onUnload() {}

    /**
     * Returns settings associated with the block.
     *
     * Shorthand for `Settings.get(getKey())`
     */
    fun getSettings(): Config
            = Settings.get(key)

    companion object {

        private val pylonEntityKeyKey = pylonKey("pylon_entity_key")

        @JvmStatic
        fun register(key: NamespacedKey, entityClass: Class<*>, pylonEntityClass: Class<out PylonEntity<*>>) {
            PylonRegistry.ENTITIES.register(PylonEntitySchema(key, entityClass, pylonEntityClass))
        }

        @JvmSynthetic
        inline fun <reified E: Entity, reified T: PylonEntity<E>> register(key: NamespacedKey) {
            PylonRegistry.ENTITIES.register(PylonEntitySchema(key, E::class.java, T::class.java))
        }

        @JvmSynthetic
        internal fun <E: Entity> initialisePylonEntity(key: NamespacedKey, entity: E): E {
            entity.persistentDataContainer.set(pylonEntityKeyKey, PylonSerializers.NAMESPACED_KEY, key)
            return entity
        }

        @JvmSynthetic
        internal fun serialize(pylonEntity: PylonEntity<*>) {
            pylonEntity.write(pylonEntity.entity.persistentDataContainer)
        }

        @JvmSynthetic
        internal fun deserialize(entity: Entity): PylonEntity<*>? {
            // Stored outside of the try block so it is displayed in error messages once acquired
            var key: NamespacedKey? = null

            try {
                key = entity.persistentDataContainer.get(pylonEntityKeyKey, PylonSerializers.NAMESPACED_KEY)
                    ?: return null

                // We fail silently here because this may trigger if an addon is removed or fails to load.
                // In this case, we don't want to delete the data, and we also don't want to spam errors.
                val schema = PylonRegistry.ENTITIES[key]
                    ?: return null

                if (!schema.entityClass.isInstance(entity)) {
                    return null
                }

                @Suppress("UNCHECKED_CAST") // The cast will work - this is checked in the schema constructor
                return schema.loadConstructor.invoke(entity) as PylonEntity<*>

            } catch (t: Throwable) {
                PylonCore.logger.severe("Error while loading entity $key with UUID ${entity.uniqueId}")
                t.printStackTrace()
                return null
            }
        }
    }
}