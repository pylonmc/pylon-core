package io.github.pylonmc.rebar.block.base

import org.bukkit.event.block.VaultDisplayItemEvent

interface PylonTrialVault {
    fun onDisplayItem(event: VaultDisplayItemEvent)
}