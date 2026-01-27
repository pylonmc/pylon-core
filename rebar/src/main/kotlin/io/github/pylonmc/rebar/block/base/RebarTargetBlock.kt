package io.github.pylonmc.rebar.block.base

import io.papermc.paper.event.block.TargetHitEvent

interface RebarTargetBlock {
    fun onHit(event: TargetHitEvent)
}