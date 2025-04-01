package io.github.pylonmc.pylon.core.block.base

import org.bukkit.event.block.SignChangeEvent

interface Sign {
    fun onSignChange(event: SignChangeEvent) {}
}