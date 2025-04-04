package io.github.pylonmc.pylon.core.entity

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent
import io.github.pylonmc.pylon.core.addon.PylonAddon
import io.github.pylonmc.pylon.core.pluginInstance
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.isFromAddon
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Entity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.EntitiesLoadEvent
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock


object EntityStorage : Listener {

    private const val AUTOSAVE_INTERVAL_TICKS = 60 * 20L

    private val entities: MutableMap<UUID, PylonEntity<*, *>> = ConcurrentHashMap()
    private val entitiesById: MutableMap<NamespacedKey, MutableSet<PylonEntity<*, *>>> = ConcurrentHashMap()

    val loadedEntities: Collection<PylonEntity<*, *>>
        get() = entities.values

    // Access to entities, entitiesById fields must be synchronized to prevent them
    // briefly going out of sync
    private val entityLock = ReentrantReadWriteLock()

    // TODO implement this properly and actually run it
    internal fun startAutosaveTask() {
        Bukkit.getScheduler().runTaskTimer(pluginInstance, Runnable {
            // TODO this saves all entities at once, potentially leading to large pauses
            for (entity in entities.values) {
                entity.write()
            }
        }, AUTOSAVE_INTERVAL_TICKS, AUTOSAVE_INTERVAL_TICKS)
    }

    @JvmStatic
    fun get(uuid: UUID): PylonEntity<*, *>?
        = lockEntityRead { entities[uuid] }

    @JvmStatic
    fun get(entity: Entity): PylonEntity<*, *>?
        = lockEntityRead { entities[entity.uniqueId] }

    @JvmStatic
    fun <T> getAs(clazz: Class<T>, uuid: UUID): T? {
        val entity = get(uuid) ?: return null
        return clazz.cast(entity)
    }

    @JvmStatic
    fun <T> getAs(clazz: Class<T>, entity: Entity): T?
        = getAs(clazz, entity.uniqueId)

    inline fun <reified T> getAs(uuid: UUID): T?
        = getAs(T::class.java, uuid)

    inline fun <reified T> getAs(entity: Entity): T?
        = getAs(T::class.java, entity)

    fun getById(id: NamespacedKey): Collection<PylonEntity<*, *>> =
        if (PylonRegistry.ENTITIES.contains(id)) {
            lockEntityRead {
                entitiesById[id].orEmpty()
            }
        } else {
            emptySet()
        }

    @JvmStatic
    fun isPylonEntity(uuid: UUID): Boolean
        = get(uuid) != null

    @JvmStatic
    fun isPylonEntity(entity: Entity): Boolean
            = get(entity) != null

    @JvmStatic
    fun add(entity: PylonEntity<*, *>) = lockEntityWrite {
        entities[entity.entity.uniqueId] = entity
        entitiesById.getOrPut(entity.schema.key) { mutableSetOf() }.add(entity)
    }

    @EventHandler
    private fun onEntityLoad(event: EntitiesLoadEvent) {
        for (entity in event.entities) {
            PylonEntity.deserialize(entity)?.let {
                add(it)
            }
        }
    }

    // This currently does not differentiate between unloaded and dead entities because the API
    // is broken (lol), hence the lack of an entity death listener
    @EventHandler
    private fun onEntityUnload(event: EntityRemoveFromWorldEvent) {
        val pylonEntity = get(event.getEntity().uniqueId) ?: return
        pylonEntity.write()
        lockEntityWrite {
            entities.remove(pylonEntity.entity.uniqueId)
            entitiesById[pylonEntity.schema.key]!!.remove(pylonEntity)
            if (entitiesById[pylonEntity.schema.key]!!.isEmpty()) {
                entitiesById.remove(pylonEntity.schema.key)
            }
        }
    }

    @JvmSynthetic
    internal fun cleanup(addon: PylonAddon) = lockEntityWrite {
        for ((_, value) in entitiesById.filter { it.key.isFromAddon(addon) }) {
            for (entity in value) {
                entity.write()
            }
        }

        entities.values.removeIf { it.schema.key.isFromAddon(addon) }
        entitiesById.keys.removeIf { it.isFromAddon(addon) }
    }

    @JvmSynthetic
    internal fun cleanupEverything() = {
        for (entity in entities.values) {
            entity.write()
        }
    }

    private inline fun <T> lockEntityRead(block: () -> T): T {
        entityLock.readLock().lock()
        try {
            return block()
        } finally {
            entityLock.readLock().unlock()
        }
    }

    private inline fun <T> lockEntityWrite(block: () -> T): T {
        entityLock.writeLock().lock()
        try {
            return block()
        } finally {
            entityLock.writeLock().unlock()
        }
    }
}