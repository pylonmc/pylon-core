package io.github.pylonmc.pylon.core.item.base

import org.bukkit.event.entity.LingeringPotionSplashEvent

interface PylonLingeringPotion {
    fun onSplash(event: LingeringPotionSplashEvent)
}