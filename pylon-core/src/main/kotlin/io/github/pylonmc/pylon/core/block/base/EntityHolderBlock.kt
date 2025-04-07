package io.github.pylonmc.pylon.core.block.base

import io.github.pylonmc.pylon.core.entity.EntityStorage
import io.github.pylonmc.pylon.core.entity.PylonEntity
import io.github.pylonmc.pylon.core.entity.PylonEntitySchema
import io.github.pylonmc.pylon.core.persistence.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.entity.Entity
import org.bukkit.persistence.PersistentDataContainer
import java.util.UUID

/**
 * A block that is tied to a Pylon entity. For example, a pedestal that uses an item
 * display to show the item would implement this to keep track of the item display.
 *
 * Note that the Pylon entity may be loaded when the block is not loaded (or vice versa).
 */
interface EntityHolderBlock<out E : PylonEntity<*, *>> : BreakHandler {

    /**
     * Must be set in your create constructor when you spawn in the entity.
     */
    var entityUuid: UUID

    @Suppress("UNCHECKED_CAST") // Unfortunately cannot use getAs here due to reified constraints
    val entity: E?
        get() = EntityStorage.get(entityUuid) as E

    fun isEntityLoaded()
        = entity != null

    /**
     * Must be called in your load constructor.
     */
    fun loadEntity(pdc: PersistentDataContainer) {
        entityUuid = pdc.get(entityKey, PylonSerializers.UUID)
            ?: throw IllegalStateException("Failed to load entity holder; did you forget to call saveEntityHolder() in your write() method?")
    }

    /**
     * Must be called in your write() method.
     */
    fun saveEntity(pdc: PersistentDataContainer) {
        pdc.set(entityKey, PylonSerializers.UUID, entityUuid)
    }

    override fun postBreak() {
        // Best-effort removal; unlikely to cause issues
        entity?.entity?.remove()
    }

    companion object {
        val entityKey = pylonKey("entity_holder_entity_uuid")
    }
}