package io.github.pylonmc.rebar.entity.base

import com.destroystokyo.paper.event.entity.ExperienceOrbMergeEvent

interface PylonExperienceOrb {
    fun onMergeOrb(event: ExperienceOrbMergeEvent) {}
    fun onAbsorbedByOrb(event: ExperienceOrbMergeEvent) {}
}