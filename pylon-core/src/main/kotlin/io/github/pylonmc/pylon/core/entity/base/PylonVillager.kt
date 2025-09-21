package io.github.pylonmc.pylon.core.entity.base

import org.bukkit.event.entity.VillagerAcquireTradeEvent
import org.bukkit.event.entity.VillagerCareerChangeEvent
import org.bukkit.event.entity.VillagerReplenishTradeEvent

interface PylonVillager {
    fun onAcquireTrade(event: VillagerAcquireTradeEvent){}
    fun onCareerChange(event: VillagerCareerChangeEvent){}
    fun onReplenishTrade(event: VillagerReplenishTradeEvent){}
}