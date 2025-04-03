package io.github.pylonmc.pylon.core.i18n

import io.github.pylonmc.pylon.core.item.PylonItem
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.text.TranslatableComponent
import org.bukkit.entity.Player

class PlayerTranslationHandler(val player: Player) {

    fun handleItem(item: PylonItem<*>) {
        val name = item.stack.getData(DataComponentTypes.ITEM_NAME)
        if (name is TranslatableComponent) {
            val key = name.key()
            Lang
        }
    }
}