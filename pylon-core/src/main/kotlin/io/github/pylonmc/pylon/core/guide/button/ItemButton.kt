package io.github.pylonmc.pylon.core.guide.button

import io.github.pylonmc.pylon.core.guide.button.ResearchButton.Companion.addResearchCostLore
import io.github.pylonmc.pylon.core.guide.pages.item.ItemRecipesPage
import io.github.pylonmc.pylon.core.guide.pages.item.ItemUsagesPage
import io.github.pylonmc.pylon.core.guide.pages.research.ResearchItemsPage
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.item.research.Research.Companion.canCraft
import io.github.pylonmc.pylon.core.item.research.Research.Companion.canUse
import io.github.pylonmc.pylon.core.item.research.Research.Companion.research
import io.github.pylonmc.pylon.core.item.research.Research.Companion.researchPoints
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.withArguments
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.item.impl.AutoCycleItem
import xyz.xenondevs.invui.item.impl.SimpleItem

class ItemButton(val stack: ItemStack) : AbstractItem() {

    constructor(key: NamespacedKey) : this(
        PylonRegistry.ITEMS[key]?.itemStack ?: throw IllegalArgumentException("There is no fluid with key $key")
    )

    @Suppress("UnstableApiUsage")
    override fun getItemProvider(player: Player): ItemProvider {
        val item = PylonItem.fromStack(stack)
        if (item == null) {
            return ItemStackBuilder.of(stack)
        }

        val placeholders = PylonItem.fromStack(stack)!!.getPlaceholders()
        val builder = ItemStackBuilder.of(stack.clone())
            .editData(DataComponentTypes.LORE) { lore ->
                ItemLore.lore(lore.lines().map { it.withArguments(placeholders) })
            }

        // buffoonery to bypass InvUI's translation mess
        // Search message 'any idea why items displayed in InvUI are not having placeholders' on Pylon's Discord for more info
        builder.editData(DataComponentTypes.ITEM_NAME) {
            it.withArguments(placeholders)
        }

        if (item.isDisabled) {
            builder.set(DataComponentTypes.ITEM_MODEL, Material.STRUCTURE_VOID.key)
        }

        if (!player.canCraft(item)) {
            builder.set(DataComponentTypes.ITEM_MODEL, Material.BARRIER.key)
                .set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, false)
                .lore(Component.translatable("pylon.pyloncore.guide.button.item.not-researched"))
            if (item.research != null) {
                addResearchCostLore(builder, player, item.research!!)
            }
            builder.lore(Component.translatable("pylon.pyloncore.guide.button.item.research-instructions"))
        }

        return builder
    }

    override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
        when (clickType) {
            ClickType.LEFT -> {
                val page = ItemRecipesPage(stack)
                if (page.pages.isNotEmpty()) {
                    page.open(player)
                }
            }
            ClickType.SHIFT_LEFT -> {
                val item = PylonItem.fromStack(stack)
                val research = item?.research
                if (item != null && research != null) {
                    if (research.isResearchedBy(player) || research.cost == null || research.cost > player.researchPoints) {
                        return
                    }
                    research.addTo(player, false)
                    player.researchPoints -= research.cost
                    windows.forEach { it.close(); it.open() } // TODO refresh windows when we've updated to 2.0.0
                }
            }
            ClickType.RIGHT -> {
                val page = ItemUsagesPage(stack)
                if (page.pages.isNotEmpty()) {
                    page.open(player)
                }
            }
            ClickType.SHIFT_RIGHT -> {
                val item = PylonItem.fromStack(stack)
                if (item != null && item.research != null && !player.canUse(item)) {
                    ResearchItemsPage(item.research!!).open(player)
                }
            }
            ClickType.MIDDLE -> {
                if (player.hasPermission("pylon.command.give")) {
                    player.setItemOnCursor(stack)
                }
            }
            else -> {}
        }
    }

    companion object {
        const val CYCLE_INTERVAL = 10

        @JvmStatic
        fun fromStack(stack: ItemStack?): Item {
            if (stack == null) {
                return SimpleItem(ItemStack(Material.AIR))
            }

            return ItemButton(stack)
        }

        @JvmStatic
        fun fromChoice(choice: RecipeChoice?): Item = when (choice) {
            is RecipeChoice.MaterialChoice -> if (choice.choices.size == 1) {
                fromStack(choice.itemStack)
            } else {
                AutoCycleItem(
                    CYCLE_INTERVAL,
                    *(choice.choices.map { ItemStackBuilder.of(it) }.toTypedArray())
                )
            }

            is RecipeChoice.ExactChoice -> if (choice.choices.size == 1) {
                fromStack(choice.itemStack)
            } else {
                AutoCycleItem(CYCLE_INTERVAL, *(choice.choices.map { ItemStackBuilder.of(it) }.toTypedArray()))
            }

            else -> SimpleItem(ItemStackBuilder.of(Material.AIR))
        }
    }
}