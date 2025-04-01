package io.github.pylonmc.pylon.core.block.base

import org.bukkit.event.block.SpongeAbsorbEvent

interface Sponge {
    fun onAbsorb(event: SpongeAbsorbEvent) {}
}