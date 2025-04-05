package io.github.pylonmc.pylon.core.entity

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent
import io.github.pylonmc.pylon.core.addon.PylonAddon
import io.github.pylonmc.pylon.core.event.PylonEntityDeathEvent
import io.github.pylonmc.pylon.core.event.PylonEntityLoadEvent
import io.github.pylonmc.pylon.core.event.PylonEntityUnloadEvent
import io.github.pylonmc.pylon.core.pluginInstance
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.isFromAddon
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Entity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.world.EntitiesLoadEvent
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock


object EntityStorage : Listener {

    private const val AUTOSAVE_INTERVAL_TICKS = 60 * 20L

    private val entities: MutableMap<UUID, PylonEntity<*, *>> = ConcurrentHashMap()
    private val entitiesByKey: MutableMap<NamespacedKey, MutableSet<PylonEntity<*, *>>> = ConcurrentHashMap()

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
        = get(entity.uniqueId)

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

    fun getByKey(key: NamespacedKey): Collection<PylonEntity<*, *>> =
        if (PylonRegistry.ENTITIES.contains(key)) {
            lockEntityRead {
                entitiesByKey[key].orEmpty()
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
        entitiesByKey.getOrPut(entity.schema.key) { mutableSetOf() }.add(entity)
    }

    @EventHandler
    private fun onEntityLoad(event: EntitiesLoadEvent) {
        for (entity in event.entities) {
            PylonEntity.deserialize(entity)?.let {
                add(it)
                PylonEntityLoadEvent(it).callEvent()
            }
        }
    }

    // This currently does not differentiate between unloaded and dead entities because the API
    // is broken (lol), hence the lack of an entity death listener
    @EventHandler
    private fun onEntityUnload(event: EntityRemoveFromWorldEvent) {
        val pylonEntity = get(event.getEntity().uniqueId) ?: return
        PylonEntity.serialize(pylonEntity)
        lockEntityWrite {
            entities.remove(pylonEntity.entity.uniqueId)
            entitiesByKey[pylonEntity.schema.key]!!.remove(pylonEntity)
            if (entitiesByKey[pylonEntity.schema.key]!!.isEmpty()) {
                entitiesByKey.remove(pylonEntity.schema.key)
            }
        }
        PylonEntityUnloadEvent(pylonEntity).callEvent()
    }

    @EventHandler(ignoreCancelled = true)
    private fun onEntityDeath(event: EntityDeathEvent)
        = get(event.entity)?.let {
            PylonEntityDeathEvent(it, event).callEvent()
        }

    @JvmSynthetic
    internal fun cleanup(addon: PylonAddon) = lockEntityWrite {
        for ((_, value) in entitiesByKey.filter { it.key.isFromAddon(addon) }) {
            for (entity in value) {
                PylonEntity.serialize(entity)
            }
        }

        entities.values.removeIf { it.schema.key.isFromAddon(addon) }
        entitiesByKey.keys.removeIf { it.isFromAddon(addon) }
    }

    @JvmSynthetic
    internal fun cleanupEverything() {
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