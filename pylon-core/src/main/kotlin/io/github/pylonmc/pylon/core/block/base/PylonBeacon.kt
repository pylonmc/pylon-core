package io.github.pylonmc.pylon.core.block.base

import com.destroystokyo.paper.event.block.BeaconEffectEvent
import io.papermc.paper.event.block.BeaconActivatedEvent
import io.papermc.paper.event.block.BeaconDeactivatedEvent
import io.papermc.paper.event.player.PlayerChangeBeaconEffectEvent

interface PylonBeacon {
    fun onActivated(event: BeaconActivatedEvent) {}
    fun onDeactivated(event: BeaconDeactivatedEvent) {}
    fun onEffectChange(event: PlayerChangeBeaconEffectEvent) {}
    fun onEffectApply(event: BeaconEffectEvent) {}
}