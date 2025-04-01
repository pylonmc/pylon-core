package io.github.pylonmc.pylon.core.block.base

import org.bukkit.event.block.TNTPrimeEvent

interface TNT {
    fun onIgnite(event: TNTPrimeEvent)
}