package io.github.pylonmc.rebar.waila

import io.github.pylonmc.rebar.config.RebarConfig
import io.github.pylonmc.rebar.waila.Waila.Companion.wailaConfig
import org.bukkit.entity.Player

class PlayerWailaConfig {
    var player: Player? = null

    var enabled: Boolean = true
        set(value) {
            field = value
            player?.wailaConfig = this
        }

    var vanillaWailaEnabled: Boolean = false
        set(value) {
            field = value
            player?.wailaConfig = this
        }

    var type: Waila.Type = RebarConfig.WailaConfig.DEFAULT_TYPE
        set(value) {
            field = value
            player?.wailaConfig = this
        }

    constructor()

    constructor(enabled: Boolean, vanillaWailaEnabled: Boolean, type: Waila.Type) {
        this.enabled = enabled
        this.vanillaWailaEnabled = vanillaWailaEnabled
        this.type = type
    }
}