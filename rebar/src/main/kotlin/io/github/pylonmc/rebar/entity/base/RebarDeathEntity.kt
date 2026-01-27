package io.github.pylonmc.rebar.entity.base

import io.github.pylonmc.rebar.event.RebarEntityDeathEvent

interface RebarDeathEntity {

    /**
     * Called when any entity is removed for any reason (except chunk unloading)
     */
    fun onDeath(event: RebarEntityDeathEvent)
}