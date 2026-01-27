package io.github.pylonmc.rebar.block.base

import org.bukkit.event.block.SpongeAbsorbEvent

interface PylonSponge {
    fun onAbsorb(event: SpongeAbsorbEvent)
}