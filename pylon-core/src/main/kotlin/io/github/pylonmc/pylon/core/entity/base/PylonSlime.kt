package io.github.pylonmc.pylon.core.entity.base

import com.destroystokyo.paper.event.entity.*
import org.bukkit.event.entity.SlimeSplitEvent

interface PylonSlime {
    fun onSwim(event: SlimeSwimEvent) {}
    fun onSplit(event: SlimeSplitEvent) {}
    fun onWander(event: SlimeWanderEvent) {}
    fun onPathfind(event: SlimePathfindEvent) {}
    fun onTarget(event: SlimeTargetLivingEntityEvent) {}
    fun onChangeDirection(event: SlimeChangeDirectionEvent) {}
}