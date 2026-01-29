package io.github.pylonmc.rebar.block.base

import org.bukkit.event.block.VaultDisplayItemEvent

interface RebarTrialVault {
    fun onDisplayItem(event: VaultDisplayItemEvent)
}