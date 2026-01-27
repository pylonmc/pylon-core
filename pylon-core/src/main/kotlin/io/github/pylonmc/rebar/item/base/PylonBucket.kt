package io.github.pylonmc.rebar.item.base

import org.bukkit.event.player.PlayerBucketEmptyEvent
import org.bukkit.event.player.PlayerBucketFillEvent

interface PylonBucket {
    /**
     * Called when the bucket is emptied.
     */
    fun onBucketEmptied(event: PlayerBucketEmptyEvent) {}

    /**
     * Called when the bucket is filled.
     */
    fun onBucketFilled(event: PlayerBucketFillEvent) {}
}