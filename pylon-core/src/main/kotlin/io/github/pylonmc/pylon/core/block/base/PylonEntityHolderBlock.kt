package io.github.pylonmc.pylon.core.block.base

import io.github.pylonmc.pylon.core.block.context.BlockBreakContext
import io.github.pylonmc.pylon.core.block.BlockStorage
import io.github.pylonmc.pylon.core.block.PhantomBlock
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.entity.EntityStorage
import io.github.pylonmc.pylon.core.entity.PylonEntity
import io.github.pylonmc.pylon.core.event.PylonBlockBreakEvent
import io.github.pylonmc.pylon.core.event.PylonBlockDeserializeEvent
import io.github.pylonmc.pylon.core.event.PylonBlockSerializeEvent
import io.github.pylonmc.pylon.core.event.PylonBlockUnloadEvent
import io.github.pylonmc.pylon.core.util.position.position
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.Bukkit
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityRemoveEvent
import org.jetbrains.annotations.ApiStatus
import java.util.IdentityHashMap
import java.util.UUID

/**
 * A block that has one or more associated Pylon entities. For example, a pedestal that
 * uses an item display to show the item would implement this to keep track of the
 * item display.
 *
 * Note that the Pylon entities may not be loaded at the same time that the block is loaded.
 */
interface PylonEntityHolderBlock {
    val block: Block

    @get:ApiStatus.NonExtendable
    val heldEntities: MutableMap<String, UUID>
        get() = holders.getOrPut(this) { mutableMapOf() }

    fun addEntity(name: String, entity: Entity) {
        heldEntities[name] = entity.uniqueId
        entity.persistentDataContainer.set(blockKey, PylonSerializers.BLOCK_POSITION, block.position)
    }

    fun addEntity(name: String, entity: PylonEntity<*>)
        = addEntity(name, entity.entity)

    @ApiStatus.NonExtendable
    fun getHeldEntityUuid(name: String) = heldEntities[name]

    @ApiStatus.NonExtendable
    fun getHeldEntityUuidOrThrow(name: String) = getHeldEntityUuid(name)
        ?: throw IllegalArgumentException("Entity $name not found")

    @ApiStatus.NonExtendable
    fun getHeldEntity(name: String): Entity?
            = getHeldEntityUuid(name)?.let { Bukkit.getEntity(it) }

    @ApiStatus.NonExtendable
    fun getHeldEntityOrThrow(name: String): Entity
            = getHeldEntity(name)
        ?: throw IllegalArgumentException("Entity $name does not exist")

    @ApiStatus.NonExtendable
    fun <T: Entity> getHeldEntity(clazz: Class<T>, name: String): T? {
        val entity = getHeldEntity(name)
        if (!clazz.isInstance(entity)) {
            return null
        }
        return clazz.cast(entity)
    }

    @ApiStatus.NonExtendable
    fun <T: Entity> getHeldEntityOrThrow(clazz: Class<T>, name: String): T
            = getHeldEntity(clazz, name)
        ?: throw IllegalArgumentException("Entity $name does not exist or is not of type ${clazz.simpleName}")

    @ApiStatus.NonExtendable
    fun <T: PylonEntity<*>> getHeldPylonEntity(clazz: Class<T>, name: String): T?
            = getHeldEntityUuid(name)?.let { EntityStorage.getAs(clazz, it) }

    @ApiStatus.NonExtendable
    fun <T: PylonEntity<*>> getHeldPylonEntityOrThrow(clazz: Class<T>, name: String): T
            = EntityStorage.getAs(clazz, getHeldEntityUuidOrThrow(name))
        ?: throw IllegalArgumentException("Entity $name is not of type ${clazz.simpleName}")

    /**
     * Returns false if the block holds no entity with the provided name, the entity is unloaded or does not
     * physically exist.
     */
    @ApiStatus.NonExtendable
    fun isHeldEntityPresent(name: String) = getHeldEntityUuid(name) != null

    /**
     * Returns false if any entity is unloaded or does not exist.
     */
    @ApiStatus.NonExtendable
    fun areAllHeldEntitiesLoaded() = heldEntities.keys.all { isHeldEntityPresent(it) }

    @ApiStatus.Internal
    companion object : Listener {
        private val entityKey = pylonKey("entity_holder_entity_uuids")
        private val blockKey = pylonKey("entity_holder_block")
        private val entityType = PylonSerializers.MAP.mapTypeFrom(PylonSerializers.STRING, PylonSerializers.UUID)

        private val holders = IdentityHashMap<PylonEntityHolderBlock, MutableMap<String, UUID>>()

        @EventHandler
        private fun onDeserialize(event: PylonBlockDeserializeEvent) {
            val block = event.pylonBlock
            if (block !is PylonEntityHolderBlock) return
            holders[block] = event.pdc.get(entityKey, entityType)?.toMutableMap()
                ?: error("Held entities not found for ${block.key}")
        }

        @EventHandler
        private fun onSerialize(event: PylonBlockSerializeEvent) {
            val block = event.pylonBlock
            if (block !is PylonEntityHolderBlock) return
            event.pdc.set(entityKey, entityType, holders[block]!!)
        }

        @EventHandler
        private fun onUnload(event: PylonBlockUnloadEvent) {
            val block = event.pylonBlock
            if (block !is PylonEntityHolderBlock) return
            holders.remove(block)
        }

        @EventHandler
        private fun onBreak(event: PylonBlockBreakEvent) {
            val block = event.pylonBlock
            if (block is PylonEntityHolderBlock) {
                // Best-effort removal; unlikely to cause issues
                block.heldEntities.values.forEach {
                    Bukkit.getEntity(it)?.let { if (it.isValid) it.remove() }
                }
                holders.remove(block)
            } else if (block is PhantomBlock) {
                block.pdc.get(entityKey, entityType)?.values?.forEach {
                    Bukkit.getEntity(it)?.let { if (it.isValid) it.remove() }
                }
            }
        }

        @EventHandler
        private fun onEntityRemove(event: EntityRemoveEvent) {
            if (event.cause == EntityRemoveEvent.Cause.UNLOAD || event.cause == EntityRemoveEvent.Cause.PLAYER_QUIT) return
            val blockPos = event.entity.persistentDataContainer.get(blockKey, PylonSerializers.BLOCK_POSITION) ?: return
            val block = BlockStorage.get(blockPos) as? PylonEntityHolderBlock ?: return
            holders[block]?.entries?.removeIf { it.value == event.entity.uniqueId }
        }
    }
}