package io.github.pylonmc.pylon.core.i18n

import io.github.pylonmc.pylon.core.item.PylonItem
import org.bukkit.entity.Player

abstract class PlayerTranslationHandler(val player: Player) {

    abstract fun handleItem(item: PylonItem<*>)
}