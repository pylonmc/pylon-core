package io.github.pylonmc.pylon.core.entity.base

import com.destroystokyo.paper.event.entity.EnderDragonFireballHitEvent

interface PylonDragonFireball {
    fun onHit(event: EnderDragonFireballHitEvent)
}