package io.github.pylonmc.pylon.core.block.base

import org.bukkit.event.entity.VillagerCareerChangeEvent

interface PylonJobBlock {
    fun onVillagerGetJob(event: VillagerCareerChangeEvent)
}