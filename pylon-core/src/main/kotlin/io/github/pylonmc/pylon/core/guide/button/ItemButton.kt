package io.github.pylonmc.pylon.core.guide.button

import com.github.shynixn.mccoroutine.bukkit.launch
import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.guide.button.ResearchButton.Companion.addResearchCostLore
import io.github.pylonmc.pylon.core.guide.pages.item.ItemRecipesPage
import io.github.pylonmc.pylon.core.guide.pages.item.ItemUsagesPage
import io.github.pylonmc.pylon.core.guide.pages.research.ResearchItemsPage
import io.github.pylonmc.pylon.core.i18n.PylonArgument
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.item.PylonItemSchema
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.item.research.Research.Companion.canCraft
import io.github.pylonmc.pylon.core.item.research.Research.Companion.canUse
import io.github.pylonmc.pylon.core.item.research.Research.Companion.researchPoints
import io.github.pylonmc.pylon.core.recipe.RecipeInput
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.withArguments
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import kotlinx.coroutines.delay
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.Registry
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.item.impl.SimpleItem
import kotlin.time.Duration.Companion.seconds

/**
 * Represents an item in the guide.
 *
 * @param stacks The items to display. If multiple are provided, the button will automatically
 * cycle through all of them. You must supply at least one item
 */
class ItemButton @JvmOverloads constructor(
    stacks: List<ItemStack>,

    /**
     * A function to apply to the button item after creating it.
     */
    val preDisplayDecorator: (ItemStack, Player) -> ItemStack = { stack, _ -> stack }
) : AbstractItem() {

    /**
     * @param stacks The items to display. If multiple are provided, the button will automatically
     * cycle through all of them. You must supply at least one item
     */
    constructor(vararg stacks: ItemStack) : this(stacks.toList())

    /**
     * @param stacks The items to display. If multiple are provided, the button will automatically
     * cycle through all of them. You must supply at least one item
     */
    constructor(stack: ItemStack, preDisplayDecorator: (ItemStack, Player) -> ItemStack) : this(listOf(stack), preDisplayDecorator)

    val stacks = stacks.shuffled()
    private var index = 0
    val currentStack: ItemStack
        get() = this.stacks[index]

    init {
        require(stacks.isNotEmpty()) { "ItemButton must have at least one ItemStack" }
        if (stacks.size > 1) {
            PylonCore.launch {
                while (true) {
                    delay(1.seconds)
                    index += 1
                    index %= stacks.size
                    notifyWindows()
                }
            }
        }
    }

    @Suppress("UnstableApiUsage")
    override fun getItemProvider(player: Player): ItemProvider {
        try {
            val displayStack = preDisplayDecorator.invoke(currentStack.clone(), player)
            val item = PylonItem.fromStack(displayStack)
            if (item == null) {
                return ItemStackBuilder.of(displayStack)
            }

            val placeholders = item.getPlaceholders()
            val builder = ItemStackBuilder.of(displayStack.clone())
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

                if (item.research != null) {
                    builder.lore(Component.translatable(
                            "pylon.pyloncore.guide.button.item.not-researched-with-name",
                            PylonArgument.of("research_name", item.research!!.name)
                    ))
                    addResearchCostLore(builder, player, item.research!!)
                } else {
                    builder.lore(Component.translatable("pylon.pyloncore.guide.button.item.not-researched"))
                }

                builder.lore(Component.translatable("pylon.pyloncore.guide.button.item.research-instructions"))
            }

            return builder
        } catch (e: Exception) {
            e.printStackTrace()
            return ItemStackBuilder.of(Material.BARRIER)
                .name(Component.translatable("pylon.pyloncore.guide.button.item.error"))
        }
    }

    override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
        try {
            when (clickType) {
                ClickType.LEFT -> {
                    val page = ItemRecipesPage(currentStack)
                    if (page.pages.isNotEmpty()) {
                        page.open(player)
                    }
                }

                ClickType.SHIFT_LEFT -> {
                    val item = PylonItem.fromStack(currentStack)
                    val research = item?.research
                    if (item != null && research != null) {
                        if (research.isResearchedBy(player) || research.cost == null || research.cost > player.researchPoints) {
                            return
                        }
                        research.addTo(player, false)
                        player.researchPoints -= research.cost
                        notifyWindows()
                    }
                }

                ClickType.RIGHT -> {
                    val page = ItemUsagesPage(currentStack)
                    if (page.pages.isNotEmpty()) {
                        page.open(player)
                    }
                }

                ClickType.SHIFT_RIGHT -> {
                    val item = PylonItem.fromStack(currentStack)
                    if (item != null && item.research != null && !player.canUse(item)) {
                        ResearchItemsPage(item.research!!).open(player)
                    }
                }

                ClickType.MIDDLE -> {
                    if (player.hasPermission("pylon.command.give")) {
                        val clonedUnkown = currentStack.clone()
                        val pylonItem = PylonItem.fromStack(clonedUnkown)

                        if (pylonItem == null) {
                            if (clonedUnkown.persistentDataContainer.isEmpty) {
                                // item is vanilla
                                val type = Registry.MATERIAL.get(clonedUnkown.type.key)!!
                                val clonedVanilla = ItemStack(type, clonedUnkown.amount)
                                player.setItemOnCursor(clonedVanilla)
                            } else {
                                // item might be special, keep it as is
                                player.setItemOnCursor(clonedUnkown)
                            }
                        } else {
                            // pylon item handling
                            val clonedPylon = pylonItem.schema.itemStack
                            clonedPylon.amount = clonedUnkown.amount
                            player.setItemOnCursor(clonedPylon)
                        }
                    }
                }

                else -> {}
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        const val CYCLE_INTERVAL = 10

        @JvmStatic
        fun from(stack: ItemStack?): Item {
            if (stack == null) {
                return SimpleItem(ItemStack(Material.AIR))
            }

            return ItemButton(stack)
        }

        @JvmStatic
        fun from(stack: ItemStack?, preDisplayDecorator: (ItemStack, Player) -> ItemStack):  Item {
            if (stack == null) {
                return SimpleItem(ItemStack(Material.AIR))
            }

            return ItemButton(listOf(stack), preDisplayDecorator)
        }

        @JvmStatic
        fun from(input: RecipeInput.Item?): Item {
            if (input == null) {
                return SimpleItem(ItemStack(Material.AIR))
            }

            return ItemButton(*input.representativeItems.toTypedArray())
        }

        @JvmStatic
        fun from(choice: RecipeChoice?): Item = when (choice) {
            is RecipeChoice.MaterialChoice -> ItemButton(choice.choices.map(::ItemStack))
            is RecipeChoice.ExactChoice -> ItemButton(choice.choices)
            else -> SimpleItem(ItemStackBuilder.of(Material.AIR))
        }
    }
}