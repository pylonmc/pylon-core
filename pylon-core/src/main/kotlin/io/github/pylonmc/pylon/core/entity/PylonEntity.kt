package io.github.pylonmc.pylon.core.entity

import io.github.pylonmc.pylon.core.persistence.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.pluginInstance
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.NamespacedKey
import org.bukkit.entity.Entity


abstract class PylonEntity<out S : PylonEntitySchema, out E: Entity> protected constructor(
    val schema: S,
    val entity: E
) {

    /**
     * Write all the state saved in the Pylon entity class to the entity's
     * persistent data container.
     */
    open fun write() {}

    companion object {

        private val pylonEntityKeyKey = pylonKey("key")

        @JvmSynthetic
        internal fun serialize(entity: PylonEntity<*, *>) {
            entity.entity.persistentDataContainer.set(pylonEntityKeyKey, PylonSerializers.NAMESPACED_KEY, entity.schema.key)
            entity.write()
        }

        @JvmSynthetic
        internal fun deserialize(entity: Entity): PylonEntity<*, *>? {
            // Stored outside of the try block so it is displayed in error messages once acquired
            var key: NamespacedKey? = null

            try {
                key = entity.persistentDataContainer.get(pylonEntityKeyKey, PylonSerializers.NAMESPACED_KEY)
                    ?: return null

                // We fail silently here because this may trigger if an addon is removed or fails to load.
                // In this case, we don't want to delete the data, and we also don't want to spam errors.
                val schema = PylonRegistry.ENTITIES[key]
                    ?: return null

                if (schema.entityClass.isInstance(entity)) {
                    return null
                }

                @Suppress("UNCHECKED_CAST") // The cast will work - this is checked in the schema constructor
                return schema.loadConstructor.invoke(schema, entity) as PylonEntity<*, *>

            } catch (t: Throwable) {
                pluginInstance.logger.severe("Error while loading entity $key with UUID ${entity.uniqueId}")
                t.printStackTrace()
                return null
            }
        }
    }
}