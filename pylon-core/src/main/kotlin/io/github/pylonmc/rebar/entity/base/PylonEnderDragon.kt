package io.github.pylonmc.rebar.entity.base

import com.destroystokyo.paper.event.entity.EnderDragonFlameEvent
import com.destroystokyo.paper.event.entity.EnderDragonShootFireballEvent
import org.bukkit.event.entity.EnderDragonChangePhaseEvent

interface PylonEnderDragon {
    fun onChangePhase(event: EnderDragonChangePhaseEvent) {}
    fun onFlame(event: EnderDragonFlameEvent) {}
    fun onShootFireball(event: EnderDragonShootFireballEvent) {}
}