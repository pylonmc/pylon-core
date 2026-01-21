package io.github.pylonmc.pylon.core.guide.button

import io.github.pylonmc.pylon.core.guide.pages.research.ResearchItemsPage
import io.github.pylonmc.pylon.core.i18n.PylonArgument
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.item.research.Research
import io.github.pylonmc.pylon.core.item.research.Research.Companion.guideHints
import io.github.pylonmc.pylon.core.item.research.Research.Companion.researchPoints
import io.github.pylonmc.pylon.core.util.getAddon
import io.github.pylonmc.pylon.core.util.pylonKey
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import xyz.xenondevs.invui.Click
import xyz.xenondevs.invui.item.AbstractItem
import xyz.xenondevs.invui.item.ItemProvider

/**
 * A button that shows a research.
 */
open class ResearchButton(val research: Research) : AbstractItem() {

    override fun getItemProvider(player: Player): ItemProvider = try {
        val playerHasResearch = Research.getResearches(player).contains(research)
        val item = ItemStackBuilder.gui(if (playerHasResearch) Material.LIME_STAINED_GLASS_PANE else research.material, "${pylonKey("research")}:${research.key}:$playerHasResearch")
            .name(research.name)

        if (playerHasResearch) {
            if (research.cost != null) {
                item.lore(Component.translatable(
                    "pylon.pyloncore.guide.button.research.cost.researched",
                    PylonArgument.of("cost", research.cost),
                ))
            }
        } else {
            addResearchCostLore(item, player, research)
        }

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

        if (player.guideHints) {
            item.lore(Component.translatable("pylon.pyloncore.guide.button.research.hints."
                    + (if (playerHasResearch) "researched" else "unresearched")
            ))
        }

        item.lore(getAddon(research.key).displayName)

        item
    } catch (e: Exception) {
        e.printStackTrace()
        ItemStackBuilder.of(Material.BARRIER)
            .name(Component.translatable("pylon.pyloncore.guide.button.fluid.error"))
    }

    override fun handleClick(clickType: ClickType, player: Player, click: Click) {
        try {
            if (clickType.isLeftClick) {
                if (research.isResearchedBy(player) || research.cost == null || research.cost > player.researchPoints) {
                    return
                }
                research.addTo(player)
                player.researchPoints -= research.cost
                notifyWindows()
            } else if (clickType.isRightClick) {
                ResearchItemsPage(research).open(player)
            } else if (clickType == ClickType.MIDDLE) {
                if (player.hasPermission("pylon.command.research.modify")) {
                    research.addTo(player)
                    notifyWindows()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        const val MAX_UNLOCK_LIST_LINES = 10

        @JvmSynthetic
        internal fun addResearchCostLore(item: ItemStackBuilder, player: Player, research: Research) {
            if (research.cost == null) {
                item.lore(Component.translatable("pylon.${research.key.namespace}.researches.${research.key.key}.unlock-instructions"))
            } else {
                val playerPoints = player.researchPoints
                item.lore(Component.translatable(
                    "pylon.pyloncore.guide.button.research.cost."
                            + (if (research.cost > playerPoints) "not-enough" else "enough"),
                    PylonArgument.of("points", playerPoints),
                    PylonArgument.of("cost", research.cost)
                ))
            }
        }
    }
}
