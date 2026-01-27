package io.github.pylonmc.rebar.entity.base

import com.destroystokyo.paper.event.entity.TurtleGoHomeEvent
import com.destroystokyo.paper.event.entity.TurtleLayEggEvent
import com.destroystokyo.paper.event.entity.TurtleStartDiggingEvent

interface PylonTurtle {
    fun onStartDigging(event: TurtleStartDiggingEvent) {}
    fun onGoHome(event: TurtleGoHomeEvent) {}
    fun onLayEgg(event: TurtleLayEggEvent) {}
}