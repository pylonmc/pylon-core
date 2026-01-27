package io.github.pylonmc.rebar.block.base

import org.bukkit.event.block.TNTPrimeEvent

interface PylonTNT {
    fun onIgnite(event: TNTPrimeEvent)
}