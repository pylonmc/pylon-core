package io.github.pylonmc.pylon.core.block.base

import io.github.pylonmc.pylon.core.block.context.BlockCreateContext
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.entity.EntityStorage
import io.github.pylonmc.pylon.core.entity.PylonEntity
import io.github.pylonmc.pylon.core.event.PylonBlockDeserializeEvent
import io.github.pylonmc.pylon.core.event.PylonBlockPlaceEvent
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
 * Note that the Pylon entities may not be loaded when the block is loaded.
 */
interface PylonEntityHolderBlock : PylonBreakHandler {

    fun createEntities(context: BlockCreateContext): Map<String, UUID>

    @get:ApiStatus.NonExtendable
    val heldEntities: MutableMap<String, UUID>
        get() = holders[this] ?: error("You cannot access held entities before the block is placed")

    @ApiStatus.NonExtendable
    fun getHeldEntityUuid(name: String) = heldEntities[name] ?: throw IllegalArgumentException("Entity $name not found")

    @ApiStatus.NonExtendable
    fun getHeldEntity(name: String): PylonEntity<*>?
        = EntityStorage.get(getHeldEntityUuid(name))

    @ApiStatus.NonExtendable
    fun <T: PylonEntity<*>> getHeldEntity(clazz: Class<T>, name: String): T?
        = EntityStorage.getAs(clazz, getHeldEntityUuid(name))

    /**
     * Returns false if the entity is unloaded or does not physically exist.
     *
     * Will error if there is no entity with the provided name.
     */
    @ApiStatus.NonExtendable
    fun isHeldEntityPresent(name: String) = EntityStorage.isPylonEntity(getHeldEntityUuid(name))

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

    companion object : Listener {
        private val entityKey = pylonKey("entity_holder_entity_uuids")
        private val entityType = PylonSerializers.MAP.mapTypeFrom(PylonSerializers.STRING, PylonSerializers.UUID)

        private val holders = IdentityHashMap<PylonEntityHolderBlock, MutableMap<String, UUID>>()

        @EventHandler
        private fun onPlace(event: PylonBlockPlaceEvent) {
            val block = event.pylonBlock
            if (block !is PylonEntityHolderBlock) return
            holders[block] = block.createEntities(event.context).toMutableMap()
        }

        @EventHandler
        private fun onDeserialize(event: PylonBlockDeserializeEvent) {
            val block = event.pylonBlock
            if (block !is PylonEntityHolderBlock) return
            holders[block] = event.data.get(entityKey, entityType)?.toMutableMap() ?: error("Held entities not found for ${block.schema.key}")
        }

        @EventHandler
        private fun onSerialize(event: PylonBlockSerializeEvent) {
            val block = event.pylonBlock
            if (block !is PylonEntityHolderBlock) return
            event.data.set(entityKey, entityType, holders[block]!!)
        }

        @EventHandler
        private fun onUnload(event: PylonBlockUnloadEvent) {
            val block = event.pylonBlock
            if (block !is PylonEntityHolderBlock) return
            holders.remove(block)
        }
    }
}