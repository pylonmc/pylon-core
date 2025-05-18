package io.github.pylonmc.pylon.core.entity

import com.google.common.base.Supplier
import io.github.pylonmc.pylon.core.block.waila.WailaConfig
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.pluginInstance
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.NamespacedKey
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataContainer


abstract class PylonEntity<out S : PylonEntitySchema, out E: Entity> protected constructor(
    val schema: S,
    val entity: E
) {

    protected constructor(schema: S, supplier: Supplier<E>): this(schema, supplier.get())

    init {
        require(PylonRegistry.ENTITIES.contains(schema.key)) {
            "You can only create entities using a registered schema; did you forget to register ${schema.key}?"
        }
    }

    val uuid = entity.uniqueId

    open fun getWaila(player: Player): WailaConfig? = null

    /**
     * Write all the state saved in the Pylon entity class to the entity's persistent data
     * container.
     */
    open fun write(pdc: PersistentDataContainer) {}

    companion object {

        private val pylonEntityKeyKey = pylonKey("entity_schema_key")

        @JvmSynthetic
        internal fun serialize(pylonEntity: PylonEntity<*, *>) {
            pylonEntity.write(pylonEntity.entity.persistentDataContainer)
            pylonEntity.entity.persistentDataContainer
                .set(pylonEntityKeyKey, PylonSerializers.NAMESPACED_KEY, pylonEntity.schema.key)
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

                if (!schema.entityClass.isInstance(entity)) {
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