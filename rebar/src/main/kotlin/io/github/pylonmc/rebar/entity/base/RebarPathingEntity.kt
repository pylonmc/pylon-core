package io.github.pylonmc.rebar.entity.base

import com.destroystokyo.paper.event.entity.EntityPathfindEvent
import org.bukkit.event.entity.EntityTargetEvent

interface RebarPathingEntity {
    fun onFindPath(event: EntityPathfindEvent) {}
    fun onTarget(event: EntityTargetEvent) {}
}