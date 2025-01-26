package io.github.pylonmc.pylon.core.item.interfaces

import org.bukkit.event.player.PlayerBucketEmptyEvent
import org.bukkit.event.player.PlayerBucketFillEvent

@FunctionalInterface
interface Bucket {
    /**
     * Called when the bucket is emptied
     */
    fun onBucketEmptied(event: PlayerBucketEmptyEvent)

    /**
     * Called when the bucket is filled
     */
    fun onBucketFilled(event: PlayerBucketFillEvent)
}