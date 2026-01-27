package io.github.pylonmc.rebar.block.base

import org.bukkit.event.block.SpongeAbsorbEvent

interface RebarSponge {
    fun onAbsorb(event: SpongeAbsorbEvent)
}