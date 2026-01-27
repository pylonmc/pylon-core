package io.github.pylonmc.rebar.entity.base

import com.destroystokyo.paper.event.entity.WitchConsumePotionEvent
import com.destroystokyo.paper.event.entity.WitchReadyPotionEvent
import com.destroystokyo.paper.event.entity.WitchThrowPotionEvent

interface PylonWitch {
    fun onConsumePotion(event: WitchConsumePotionEvent) {}
    fun onReadyPotion(event: WitchReadyPotionEvent) {}
    fun onThrowPotion(event: WitchThrowPotionEvent) {}
}