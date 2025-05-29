package io.github.pylonmc.pylon.core.entity

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.addon.PylonAddon
import io.github.pylonmc.pylon.core.config.PylonConfig
import io.github.pylonmc.pylon.core.event.PylonEntityDeathEvent
import io.github.pylonmc.pylon.core.event.PylonEntityLoadEvent
import io.github.pylonmc.pylon.core.event.PylonEntityUnloadEvent
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.isFromAddon
import io.github.pylonmc.pylon.core.util.position.position
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Entity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityRemoveEvent
import org.bukkit.event.world.EntitiesLoadEvent
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.random.Random


object EntityStorage : Listener {

    private val random = Random(System.nanoTime())

    private val entities: MutableMap<UUID, PylonEntity<*>> = ConcurrentHashMap()
    private val entitiesByKey: MutableMap<NamespacedKey, MutableSet<PylonEntity<*>>> = ConcurrentHashMap()
    private val entityAutosaveTasks: MutableMap<UUID, Job> = ConcurrentHashMap()

    val loadedEntities: Collection<PylonEntity<*>>
        get() = entities.values

    // Access to entities, entitiesById fields must be synchronized to prevent them
    // briefly going out of sync
    private val entityLock = ReentrantReadWriteLock()

    @JvmStatic
    fun get(uuid: UUID): PylonEntity<*>?
        = lockEntityRead { entities[uuid] }

    @JvmStatic
    fun get(entity: Entity): PylonEntity<*>?
        = get(entity.uniqueId)

    @JvmStatic
    fun <T> getAs(clazz: Class<T>, uuid: UUID): T? {
        val entity = get(uuid) ?: return null
        if (!clazz.isInstance(entity)) {
            return null
        }
        return clazz.cast(entity)
    }

    @JvmStatic
    fun <T> getAs(clazz: Class<T>, entity: Entity): T?
        = getAs(clazz, entity.uniqueId)

    inline fun <reified T> getAs(uuid: UUID): T?
        = getAs(T::class.java, uuid)

    inline fun <reified T> getAs(entity: Entity): T?
        = getAs(T::class.java, entity)

    fun getByKey(key: NamespacedKey): Collection<PylonEntity<*>> =
        if (key in PylonRegistry.ENTITIES) {
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
    fun add(entity: PylonEntity<*>) = lockEntityWrite {
        entities[entity.uuid] = entity
        entitiesByKey.getOrPut(entity.schema.key, ::mutableSetOf).add(entity)

        // autosaving
        entityAutosaveTasks[entity.uuid] = PylonCore.launch(PylonCore.minecraftDispatcher) {

            // Wait a random delay before starting, this is to help smooth out lag from saving
            delay(random.nextLong(PylonConfig.entityDataAutosaveIntervalSeconds * 1000))

            entityAutosaveTasks[entity.uuid] = PylonCore.launch(PylonCore.minecraftDispatcher) {
                lockEntityRead {
                    entity.write(entity.entity.persistentDataContainer)
                }
                delay(PylonConfig.entityDataAutosaveIntervalSeconds * 1000)
            }
        }
    }

    @EventHandler
    private fun onEntityLoad(event: EntitiesLoadEvent) {
        for (entity in event.entities) {
            val pylonEntity = PylonEntity.deserialize(entity) ?: continue
            add(pylonEntity)
            PylonEntityLoadEvent(pylonEntity).callEvent()
        }
    }

    // This currently does not differentiate between unloaded and dead entities because the API
    // is broken (lol), hence the lack of an entity death listener
    @EventHandler
    private fun onEntityUnload(event: EntityRemoveFromWorldEvent) {
        val pylonEntity = get(event.entity.uniqueId) ?: return

        if (!event.entity.isDead) {
            PylonEntity.serialize(pylonEntity)
            PylonEntityUnloadEvent(pylonEntity).callEvent()
        } else {
            PylonEntityDeathEvent(pylonEntity, event).callEvent()
        }

        lockEntityWrite {
            entities.remove(pylonEntity.uuid)
            entitiesByKey[pylonEntity.schema.key]!!.remove(pylonEntity)
            if (entitiesByKey[pylonEntity.schema.key]!!.isEmpty()) {
                entitiesByKey.remove(pylonEntity.schema.key)
            }
            entityAutosaveTasks.remove(pylonEntity.uuid)
        }
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
            entity.write(entity.entity.persistentDataContainer)
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