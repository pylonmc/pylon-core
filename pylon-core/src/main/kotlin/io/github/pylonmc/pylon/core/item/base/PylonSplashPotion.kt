package io.github.pylonmc.pylon.core.item.base

import org.bukkit.event.entity.PotionSplashEvent

interface PylonSplashPotion {
    /**
     * Called when the potion hits the ground and 'splashes.'
     */
    fun onSplash(event: PotionSplashEvent)
}