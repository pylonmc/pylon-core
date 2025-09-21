package io.github.pylonmc.pylon.core.entity.base

import com.destroystokyo.paper.event.entity.SlimeChangeDirectionEvent
import com.destroystokyo.paper.event.entity.SlimePathfindEvent
import com.destroystokyo.paper.event.entity.SlimeSwimEvent
import com.destroystokyo.paper.event.entity.SlimeTargetLivingEntityEvent
import com.destroystokyo.paper.event.entity.SlimeWanderEvent
import org.bukkit.event.entity.SlimeSplitEvent

interface PylonSlime {
    fun onSwim(event: SlimeSwimEvent){}
    fun onSplit(event: SlimeSplitEvent){}
    fun onWander(event: SlimeWanderEvent){}
    fun onPathfind(event: SlimePathfindEvent){}
    fun onTarget(event: SlimeTargetLivingEntityEvent){}
    fun onChangeDirection(event: SlimeChangeDirectionEvent){}
}