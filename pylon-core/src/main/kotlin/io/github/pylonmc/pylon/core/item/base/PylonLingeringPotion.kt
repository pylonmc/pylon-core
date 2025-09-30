package io.github.pylonmc.pylon.core.item.base

import org.bukkit.event.entity.LingeringPotionSplashEvent

interface PylonLingeringPotion {
    /**
     * Called when the potion hits the ground and 'splashes.'
     */
    fun onSplash(event: LingeringPotionSplashEvent)
}