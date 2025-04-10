package io.github.pylonmc.pylon.core.block.base

import org.bukkit.event.block.VaultDisplayItemEvent

interface PylonTrialVault {
    fun onDisplayItem(event: VaultDisplayItemEvent)
}