package io.github.pylonmc.pylon.core.block.base

import org.bukkit.event.block.SpongeAbsorbEvent

interface PylonSponge {
    fun onAbsorb(event: SpongeAbsorbEvent) {}
}