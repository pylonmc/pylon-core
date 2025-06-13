package io.github.pylonmc.pylon.core.guide.button

import io.github.pylonmc.pylon.core.guide.pages.ResearchUnlocksPage
import io.github.pylonmc.pylon.core.i18n.PylonArgument
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.key.getAddon
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
        // TODO make it green if researched
        val item = ItemStackBuilder.of(research.material)
            .name(research.name)

        if (research.cost == null) {
            item.lore(Component.translatable("pylon.${research.key.namespace}.researches.${research.key.key}.unlock-instructions"))
        } else {
            val playerPoints = 5 // TODO why the fuck does this not work, fuck you intellj
            item.lore(Component.translatable(
                "pylon.pyloncore.guide.button.research.cost."
                        + (if (research.cost > playerPoints) "not-enough" else "enough"),
                PylonArgument.of("points", playerPoints),
                PylonArgument.of("cost", research.cost)
            ))
        }

        item.lore(Component.translatable("pylon.pyloncore.guide.button.research.instructions"))

        item.lore(Component.translatable("pylon.pyloncore.guide.button.research.unlocks-title"))

        val shouldCutOff = research.unlocks.size > MAX_UNLOCK_LIST_LINES
        var itemListCount = if (shouldCutOff) {
            MAX_UNLOCK_LIST_LINES - 1
        } else {
            research.unlocks.size
        }

        var i = 0
        for (researchItemKey in research.unlocks) {
            if (i >= itemListCount) {
                break
            }
            i++

            item.lore(Component.translatable(
                "pylon.pyloncore.guide.button.research.unlocks-item",
                PylonArgument.of("item", Component.translatable("pylon.${researchItemKey.namespace}.item.${researchItemKey.key}.name"))
            ))
        }

        if (shouldCutOff) {
            item.lore(Component.translatable(
                "pylon.pyloncore.guide.button.research.more-researches",
                PylonArgument.of("amount", research.unlocks.size - i)
            ))
        }

        item.lore(getAddon(key).displayName)

        return item
    }

    override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
        if (clickType.isLeftClick) {
            // TODO research logic when intellij isn't waging a war against me
        } else if (clickType.isRightClick) {
            ResearchUnlocksPage(research).open(player)
        }
    }

    companion object {
        const val MAX_UNLOCK_LIST_LINES = 10
    }
}
