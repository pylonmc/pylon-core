package io.github.pylonmc.rebar.entity.base

import com.destroystokyo.paper.event.entity.EnderDragonFireballHitEvent

interface RebarDragonFireball {
    fun onHit(event: EnderDragonFireballHitEvent)
}