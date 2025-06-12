package io.github.pylonmc.pylon.core.guide.button

import io.github.pylonmc.pylon.core.i18n.PylonArgument
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.item.research.Research
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import net.kyori.adventure.text.Component
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AbstractItem

// TODO subcategory by addon
open class ResearchButton(val key: NamespacedKey) : AbstractItem() {

    val research = PylonRegistry.RESEARCHES[key] ?: throw IllegalArgumentException("There is no item with key $key")

    override fun getItemProvider(): ItemProvider {
        val item = ItemStackBuilder.of(research.material)
            .name(research.name)

        if (research.cost == null) {
            item.lore(Component.translatable("pylon.${research.key.namespace}.researches.${research.key.key}.unlock-instructions"))
        } else {
            val playerPoints = 5 // TODO why the fuck does this not work, fuck you intellj
            item.lore(Component.translatable(
                "pylon.pyloncore.guide.page.researches.research-button.cost."
                        + (if (research.cost < playerPoints) "not-enough" else "enough"),
                PylonArgument.of("points", playerPoints),
                PylonArgument.of("cost", research.cost)
            ))
        }

        item.lore(Component.translatable("pylon.pyloncore.guide.page.researches.research-button.instructions"))

        return item
    }

    override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
        if (clickType.isLeftClick) {
            // TODO research logic
        } else if (clickType.isRightClick) {
            // TODO open usage list
        }
    }
}
