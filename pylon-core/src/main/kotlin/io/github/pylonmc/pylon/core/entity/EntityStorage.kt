package io.github.pylonmc.pylon.core.entity

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent
import com.github.shynixn.mccoroutine.bukkit.launch
import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.addon.PylonAddon
import io.github.pylonmc.pylon.core.config.PylonConfig
import io.github.pylonmc.pylon.core.event.PylonEntityDeathEvent
import io.github.pylonmc.pylon.core.event.PylonEntityLoadEvent
import io.github.pylonmc.pylon.core.event.PylonEntityUnloadEvent
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.isFromAddon
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import org.bukkit.NamespacedKey
import org.bukkit.entity.Entity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.EntitiesLoadEvent
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import java.util.function.Consumer
import kotlin.random.Random


object EntityStorage : Listener {

    private val entities: MutableMap<UUID, PylonEntity<*>> = ConcurrentHashMap()
    private val entitiesByKey: MutableMap<NamespacedKey, MutableSet<PylonEntity<*>>> = ConcurrentHashMap()
    private val entityAutosaveTasks: MutableMap<UUID, Job> = ConcurrentHashMap()
    private val whenEntityLoadsTasks: MutableMap<UUID, MutableSet<Consumer<PylonEntity<*>>>> = ConcurrentHashMap()

    val loadedEntities: Collection<PylonEntity<*>>
        get() = entities.values

    // Access to entities, entitiesById fields must be synchronized to prevent them
    // briefly going out of sync
    private val entityLock = ReentrantReadWriteLock()

    @JvmStatic
    fun get(uuid: UUID): PylonEntity<*>? = lockEntityRead { entities[uuid] }

    @JvmStatic
    fun get(entity: Entity): PylonEntity<*>? = get(entity.uniqueId)

    @JvmStatic
    fun <T> getAs(clazz: Class<T>, uuid: UUID): T? {
        val entity = get(uuid) ?: return null
        if (!clazz.isInstance(entity)) {
            return null
        }
        return clazz.cast(entity)
    }

    @JvmStatic
    fun <T> getAs(clazz: Class<T>, entity: Entity): T? = getAs(clazz, entity.uniqueId)

    inline fun <reified T> getAs(uuid: UUID): T? = getAs(T::class.java, uuid)

    inline fun <reified T> getAs(entity: Entity): T? = getAs(T::class.java, entity)

    @JvmStatic
    fun getByKey(key: NamespacedKey): Collection<PylonEntity<*>> =
        if (key in PylonRegistry.ENTITIES) {
            lockEntityRead {
                entitiesByKey[key].orEmpty()
            }
        } else {
            emptySet()
        }

    /**
     * Schedules a task to run when the entity with id [uuid] is loaded, or runs the task immediately
     * if the entity is already loaded
     */
    @JvmStatic
    fun whenEntityLoads(uuid: UUID, consumer: Consumer<PylonEntity<*>>) {
        val pylonEntity = get(uuid)
        if (pylonEntity != null) {
            consumer.accept(pylonEntity)
        } else {
            whenEntityLoadsTasks.getOrPut(uuid) { mutableSetOf() }.add {
                consumer.accept(it)
            }
        }

    }

    /**
     * Schedules a task to run when the entity with id [uuid] is loaded, or runs the task immediately
     * if the entity is already loaded
     */
    @JvmStatic
    fun <T : PylonEntity<*>> whenEntityLoads(uuid: UUID, clazz: Class<T>, consumer: Consumer<T>) {
        val pylonEntity = getAs(clazz, uuid)
        if (pylonEntity != null) {
            consumer.accept(pylonEntity)
        } else {
            whenEntityLoadsTasks.getOrPut(uuid) { mutableSetOf() }.add {
                consumer.accept(
                    getAs(clazz, uuid)
                        ?: throw IllegalStateException("Entity $uuid was not of expected type ${clazz.simpleName}")
                )
            }
        }
    }

    /**
     * Schedules a task to run when the entity with id [uuid] is loaded, or runs the task immediately
     * if the entity is already loaded
     */
    @JvmStatic
    inline fun <reified T : PylonEntity<*>> whenEntityLoads(uuid: UUID, noinline consumer: (T) -> Unit) =
        whenEntityLoads(uuid, T::class.java, consumer)

    @JvmStatic
    fun isPylonEntity(uuid: UUID): Boolean = get(uuid) != null

    @JvmStatic
    fun isPylonEntity(entity: Entity): Boolean = get(entity) != null

    @JvmStatic
    fun <E : Entity> add(entity: PylonEntity<E>): PylonEntity<E> = lockEntityWrite {
        entities[entity.uuid] = entity
        entitiesByKey.getOrPut(entity.schema.key, ::mutableSetOf).add(entity)

        // autosaving
        entityAutosaveTasks[entity.uuid] = PylonCore.launch {

            // Wait a random delay before starting, this is to help smooth out lag from saving
            delay(Random.nextLong(PylonConfig.entityDataAutosaveIntervalSeconds * 1000))

            while (true) {
                lockEntityWrite {
                    entity.save()
                }
                delay(PylonConfig.entityDataAutosaveIntervalSeconds * 1000)
            }
        }
        entity
    }

    @EventHandler
    private fun onEntityLoad(event: EntitiesLoadEvent) {
        for (entity in event.entities) {
            val pylonEntity = RealPylonEntity.deserialize(entity) ?: continue
            add(pylonEntity)

            val tasks = whenEntityLoadsTasks[pylonEntity.uuid]
            if (tasks != null) {
                for (task in tasks) {
                    try {
                        task.accept(pylonEntity)
                    } catch (t: Throwable) {
                        t.printStackTrace()
                    }
                }
                whenEntityLoadsTasks.remove(pylonEntity.uuid)
            }

            PylonEntityLoadEvent(pylonEntity).callEvent()
        }
    }

    // This currently does not differentiate between unloaded and dead entities because the API
    // is broken (lol), hence the lack of an entity death listener
    @EventHandler
    private fun onEntityUnload(event: EntityRemoveFromWorldEvent) {
        val pylonEntity = get(event.entity.uniqueId) ?: return

        if (!event.entity.isDead) {
            pylonEntity.save()
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
            entityAutosaveTasks.remove(pylonEntity.uuid)?.cancel()
        }
    }

    @JvmSynthetic
    internal fun cleanup(addon: PylonAddon) = lockEntityWrite {
        for ((_, value) in entitiesByKey.filter { it.key.isFromAddon(addon) }) {
            for (entity in value) {
                entity.save()
            }
        }

        entities.values.removeIf { it.schema.key.isFromAddon(addon) }
        entitiesByKey.keys.removeIf { it.isFromAddon(addon) }
    }

    @JvmSynthetic
    internal fun cleanupEverything() {
        for (entity in entities.values) {
            entity.save()
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