package io.github.pylonmc.rebar.entity.base

import com.destroystokyo.paper.event.entity.EntityJumpEvent
import io.papermc.paper.event.entity.EntityKnockbackEvent
import io.papermc.paper.event.entity.EntityMoveEvent
import io.papermc.paper.event.entity.EntityToggleSitEvent
import org.bukkit.event.entity.EntityToggleGlideEvent
import org.bukkit.event.entity.EntityToggleSwimEvent

interface PylonMovingEntity {
    fun onMove(event: EntityMoveEvent) {}
    fun onJump(event: EntityJumpEvent) {}
    fun onKnockback(event: EntityKnockbackEvent) {}
    fun onToggleSwim(event: EntityToggleSwimEvent) {}
    fun onToggleGlide(event: EntityToggleGlideEvent) {}
    fun onToggleSit(event: EntityToggleSitEvent) {}
}