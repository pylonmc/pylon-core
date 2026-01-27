package io.github.pylonmc.rebar.entity.base

import com.destroystokyo.paper.event.entity.EnderDragonFireballHitEvent

interface PylonDragonFireball {
    fun onHit(event: EnderDragonFireballHitEvent)
}