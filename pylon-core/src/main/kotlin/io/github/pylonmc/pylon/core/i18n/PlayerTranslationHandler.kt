package io.github.pylonmc.pylon.core.i18n

import io.github.pylonmc.pylon.core.item.PylonItem
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.translation.GlobalTranslator
import org.bukkit.entity.Player

@Suppress("UnstableApiUsage")
class PlayerTranslationHandler(val player: Player) {

    fun handleItem(item: PylonItem<*>) {
        val name = item.stack.getData(DataComponentTypes.ITEM_NAME) ?: return
        val translated = GlobalTranslator.render(name, player.locale())
        item.stack.setData(DataComponentTypes.ITEM_NAME, translated)
    }
}