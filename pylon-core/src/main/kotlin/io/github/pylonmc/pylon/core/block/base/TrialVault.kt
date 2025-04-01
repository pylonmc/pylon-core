package io.github.pylonmc.pylon.core.block.base

import org.bukkit.event.block.VaultDisplayItemEvent

interface TrialVault {
    fun onDisplayItem(event: VaultDisplayItemEvent) {}
}