package io.github.pylonmc.pylon.core.item.base

import org.bukkit.event.entity.PotionSplashEvent

interface PylonSplashPotion {
    fun onSplash(event: PotionSplashEvent)
}