package io.github.pylonmc.pylon.core.entity

import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.block.waila.WailaConfig
import io.github.pylonmc.pylon.core.config.Config
import io.github.pylonmc.pylon.core.config.Settings
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.NamespacedKey
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataContainer


abstract class PylonEntity<out E: Entity>(val entity: E) {

    val key = entity.persistentDataContainer.get(pylonEntityKeyKey, PylonSerializers.NAMESPACED_KEY)
        ?: throw IllegalStateException("Entity did not have a Pylon key; did you mean to call PylonEntity(NamespacedKey, Entity) instead of PylonEntity(Entity)?")
    val schema = PylonRegistry.ENTITIES.getOrThrow(key)
    val uuid = entity.uniqueId

    constructor(key: NamespacedKey, entity: E): this(initialisePylonEntity<E>(key, entity))

    /**
     * This will only be called for the player if the player has WAILA enabled
     *
     * @return the WAILA configuration, or null if WAILA should not be shown for this block
     */
    open fun getWaila(player: Player): WailaConfig? = null

    /**
     * Write all the state saved in the Pylon entity class to the entity's persistent data
     * container.
     */
    open fun write(pdc: PersistentDataContainer) {}

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