package io.github.pylonmc.pylon.core.block.base

import io.github.pylonmc.pylon.core.entity.EntityStorage
import io.github.pylonmc.pylon.core.entity.PylonEntity
import io.github.pylonmc.pylon.core.persistence.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.persistence.PersistentDataContainer
import java.util.UUID
import javax.annotation.CheckReturnValue
import javax.annotation.OverridingMethodsMustInvokeSuper

/**
 * A block that has one or more associated Pylon entities. For example, a pedestal that
 * uses an item display to show the item would implement this to keep track of the
 * item display.
 *
 * Note that the Pylon entities may not be loaded when the block is loaded.
 */
interface PylonEntityHolderBlock : PylonBreakHandler {

    /**
     * Must be set in your create constructor when you spawn in the entity.
     */
    val heldEntities: Map<String, UUID>

    fun getHeldEntityUuid(name: String)
        = heldEntities[name] ?: throw IllegalArgumentException("Entity $name not found")

    fun getHeldEntity(name: String): PylonEntity<*, *>?
        = EntityStorage.get(getHeldEntityUuid(name))

    fun <T: PylonEntity<*, *>> getHeldEntity(clazz: Class<T>, name: String): T?
        = EntityStorage.getAs(clazz, getHeldEntityUuid(name))

    /**
     * Returns false if the entity is unloaded or does not physically exist.
     *
     * Will error if there is no entity with the provided name.
     */
    fun isHeldEntityPresent(name: String)
        = EntityStorage.isPylonEntity(getHeldEntityUuid(name))

    /**
     * Returns false if any entity is unloaded or does not exist.
     */
    fun areAllHeldEntitiesLoaded()
        = heldEntities.keys.all { isHeldEntityPresent(it) }

    /**
     * Must be called in your load constructor.
     */
    @CheckReturnValue
    fun loadHeldEntities(pdc: PersistentDataContainer): Map<String, UUID> {
        return pdc.get(entityKey, PylonSerializers.MAP.mapTypeFrom(PylonSerializers.STRING, PylonSerializers.UUID))
            ?: throw IllegalStateException("Failed to load entity holder; did you forget to call saveHeldEntities() in your write() method?")
    }

    /**
     * Must be called in your write() method.
     */
    fun saveHeldEntities(pdc: PersistentDataContainer) {
        pdc.set(entityKey, PylonSerializers.MAP.mapTypeFrom(PylonSerializers.STRING, PylonSerializers.UUID), heldEntities)
    }

    @OverridingMethodsMustInvokeSuper
    override fun postBreak() {
        // Best-effort removal; unlikely to cause issues
        for (name in heldEntities.keys) {
            getHeldEntity(name)?.entity?.remove()
        }
    }

    companion object {
        val entityKey = pylonKey("entity_holder_entity_uuids")
    }
}