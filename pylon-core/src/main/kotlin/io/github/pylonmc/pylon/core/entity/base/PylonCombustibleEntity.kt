package io.github.pylonmc.pylon.core.entity.base

import org.bukkit.event.entity.EntityCombustEvent

interface PylonCombustibleEntity {
    /* Could either be a EntityCombustByBlock or a EntityCombustByEntity event */
    fun onCombust(event: EntityCombustEvent)
}
