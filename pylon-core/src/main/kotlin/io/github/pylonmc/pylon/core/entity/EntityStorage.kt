package io.github.pylonmc.pylon.core.entity

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent
import io.github.pylonmc.pylon.core.pluginInstance
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

    val entities: MutableMap<UUID, PylonEntity<*, *>> = ConcurrentHashMap()
    val entitiesByType: MutableMap<NamespacedKey, MutableSet<PylonEntity<*, *>>> = ConcurrentHashMap()

    // Access to blocks, blocksByChunk, blocksById fields must be synchronized
    // to prevent them briefly going out of sync
    private val blockLock = ReentrantReadWriteLock()

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
        = lockBlockRead { entities[uuid] }

    @JvmStatic
    fun get(entity: Entity): PylonEntity<*, *>?
        = lockBlockRead { entities[entity.uniqueId] }

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

    @EventHandler
    private fun onEntityLoad(event: EntitiesLoadEvent) {
        for (entity in event.entities) {
            PylonEntity.deserialize(entity)?.let { pylonEntity -> lockBlockWrite {
                entities[entity.uniqueId] = pylonEntity
                entitiesByType.getOrPut(pylonEntity.schema.key) { mutableSetOf() }.add(pylonEntity)
            }}
        }
    }

    // This currently does not differentiate between unloaded and dead entities because the API
    // is broken (lol), hence the lack of an entity death listener
    @EventHandler
    private fun onEntityUnload(event: EntityRemoveFromWorldEvent) {
        val pylonEntity = get(event.getEntity().uniqueId) ?: return
        pylonEntity.write()
        lockBlockWrite {
            entities.remove(pylonEntity.entity.uniqueId)
            entitiesByType[pylonEntity.schema.key]!!.remove(pylonEntity)
            if (entitiesByType[pylonEntity.schema.key]!!.isEmpty()) {
                entitiesByType.remove(pylonEntity.schema.key)
            }
        }
    }

    private inline fun <T> lockBlockRead(block: () -> T): T {
        blockLock.readLock().lock()
        try {
            return block()
        } finally {
            blockLock.readLock().unlock()
        }
    }

    private inline fun <T> lockBlockWrite(block: () -> T): T {
        blockLock.writeLock().lock()
        try {
            return block()
        } finally {
            blockLock.writeLock().unlock()
        }
    }
}