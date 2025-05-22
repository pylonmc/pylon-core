package io.github.pylonmc.pylon.core.entity.base

import io.github.pylonmc.pylon.core.event.PylonEntityDeathEvent

interface PylonDeathEntity {

    /**
     * Called when any entity is removed for any reason (except chunk unloading)
     */
    fun onDeath(event: PylonEntityDeathEvent)
}