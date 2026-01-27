package io.github.pylonmc.rebar.block.base

import org.bukkit.event.block.TNTPrimeEvent

interface RebarTNT {
    fun onIgnite(event: TNTPrimeEvent)
}