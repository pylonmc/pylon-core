package io.github.pylonmc.pylon.core.block.base

import io.papermc.paper.event.block.TargetHitEvent

interface TargetBlock {
    fun onHit(event: TargetHitEvent)
}