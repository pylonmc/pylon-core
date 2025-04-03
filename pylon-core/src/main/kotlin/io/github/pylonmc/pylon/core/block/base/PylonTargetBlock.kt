package io.github.pylonmc.pylon.core.block.base

import io.papermc.paper.event.block.TargetHitEvent

interface PylonTargetBlock {
    fun onHit(event: TargetHitEvent) {}
}