package io.github.pylonmc.pylon.core.waila

import io.github.pylonmc.pylon.core.config.PylonConfig
import io.github.pylonmc.pylon.core.waila.Waila.Companion.wailaConfig
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

    var type: Waila.Type = PylonConfig.WailaConfig.DEFAULT_TYPE
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