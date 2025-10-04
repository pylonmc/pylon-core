package io.github.pylonmc.pylon.core.block.base

import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.entity.EntityStorage
import io.github.pylonmc.pylon.core.entity.PylonEntity
import io.github.pylonmc.pylon.core.event.PylonBlockDeserializeEvent
import io.github.pylonmc.pylon.core.event.PylonBlockSerializeEvent
import io.github.pylonmc.pylon.core.event.PylonBlockUnloadEvent
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.annotations.MustBeInvokedByOverriders
import java.util.IdentityHashMap
import java.util.UUID

/**
 * A block that has one or more associated Pylon entities. For example, a pedestal that
 * uses an item display to show the item would implement this to keep track of the
 * item display.
 *
 * Note that the Pylon entities may not be loaded at the same time that the block is loaded.
 */
interface PylonEntityHolderBlock : PylonBreakHandler {

    @get:ApiStatus.NonExtendable
    val heldEntities: MutableMap<String, UUID>
        get() = holders.getOrPut(this) { mutableMapOf() }

    fun addEntity(name: String, entity: PylonEntity<*>) {
        if (!EntityStorage.isPylonEntity(entity.uuid)) {
            EntityStorage.add(entity)
        }
        heldEntities[name] = entity.uuid
    }

    @ApiStatus.NonExtendable
    fun getHeldEntityUuid(name: String) = heldEntities[name]

    @ApiStatus.NonExtendable
    fun getHeldEntityUuidOrThrow(name: String) = getHeldEntityUuid(name)
        ?: throw IllegalArgumentException("Entity $name not found")

    @ApiStatus.NonExtendable
    fun getHeldEntity(name: String): PylonEntity<*>?
            = getHeldEntityUuid(name)?.let { EntityStorage.get(it) }

    @ApiStatus.NonExtendable
    fun getHeldEntityOrThrow(name: String): PylonEntity<*>
            = getHeldEntity(name)
        ?: throw IllegalArgumentException("Entity $name does not exist")

    @ApiStatus.NonExtendable
    fun <T: PylonEntity<*>> getHeldEntity(clazz: Class<T>, name: String): T?
            = getHeldEntityUuid(name)?.let { EntityStorage.getAs(clazz, it) }

    @ApiStatus.NonExtendable
    fun <T: PylonEntity<*>> getHeldEntityOrThrow(clazz: Class<T>, name: String): T
            = getHeldEntity(clazz, name)
        ?: throw IllegalArgumentException("Entity $name does not exist or is not of type ${clazz.simpleName}")

    /**
     * Returns false if the block holds no entity with the provided name, the entity is unloaded or does not physically exist.
     */
    @ApiStatus.NonExtendable
    fun isHeldEntityPresent(name: String) = getHeldEntityUuid(name)?.let { EntityStorage.isPylonEntity(it) } == true

    /**
     * Returns false if any entity is unloaded or does not exist.
     */
    @ApiStatus.NonExtendable
    fun areAllHeldEntitiesLoaded() = heldEntities.keys.all { isHeldEntityPresent(it) }

    @MustBeInvokedByOverriders
    override fun postBreak() {
        // Best-effort removal; unlikely to cause issues
        for (name in heldEntities.keys) {
            getHeldEntity(name)?.entity?.remove()
        }
    }

    @ApiStatus.Internal
    companion object : Listener {
        private val entityKey = pylonKey("entity_holder_entity_uuids")
        private val entityType = PylonSerializers.MAP.mapTypeFrom(PylonSerializers.STRING, PylonSerializers.UUID)

        private val holders = IdentityHashMap<PylonEntityHolderBlock, MutableMap<String, UUID>>()

        @EventHandler
        private fun onDeserialize(event: PylonBlockDeserializeEvent) {
            val block = event.pylonBlock
            if (block !is PylonEntityHolderBlock) return
            holders[block] = event.pdc.get(entityKey, entityType)?.toMutableMap() ?: error("Held entities not found for ${block.key}")
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
    }
}