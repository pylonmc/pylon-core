package io.github.pylonmc.pylon.core.guide.button

import com.github.shynixn.mccoroutine.bukkit.launch
import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.fluid.PylonFluid
import io.github.pylonmc.pylon.core.guide.pages.fluid.FluidRecipesPage
import io.github.pylonmc.pylon.core.guide.pages.fluid.FluidUsagesPage
import io.github.pylonmc.pylon.core.i18n.PylonArgument
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.recipe.RecipeInput
import io.github.pylonmc.pylon.core.util.gui.unit.UnitFormat
import io.papermc.paper.datacomponent.DataComponentTypes
import kotlinx.coroutines.delay
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import xyz.xenondevs.invui.item.impl.AbstractItem
import kotlin.time.Duration.Companion.seconds

open class FluidButton(
    fluids: List<PylonFluid>,
    val amount: Double?,
    val preDisplayDecorator: (ItemStackBuilder) -> ItemStackBuilder
) : AbstractItem() {
    constructor(amount: Double?, vararg fluids: PylonFluid) : this(fluids.toList(), amount, { it })
    constructor(vararg fluids: PylonFluid) : this(null, *fluids)
    constructor(input: RecipeInput.Fluid) : this(input.amountMillibuckets, *input.fluids.toTypedArray())

    val fluids = fluids.shuffled()
    private var index = 0
    val currentFluid: PylonFluid
        get() = this.fluids[index]

    init {
        require(fluids.isNotEmpty()) { "Fluids list cannot be empty" }
        if (fluids.size > 1) {
            PylonCore.launch {
                while (true) {
                    delay(1.seconds)
                    index += 1
                    index %= fluids.size
                    notifyWindows()
                }
            }
        }
    }

    override fun getItemProvider() = try {
        if (amount == null) {
            preDisplayDecorator.invoke(currentFluid.getItem())
        } else {
            preDisplayDecorator.invoke(currentFluid.getItem())
                .name(
                    Component.translatable(
                        "pylon.pyloncore.guide.button.fluid.name",
                        PylonArgument.of("fluid", currentFluid.getItem().stack.getData(DataComponentTypes.ITEM_NAME)!!),
                        PylonArgument.of("amount", UnitFormat.MILLIBUCKETS.format(amount).decimalPlaces(2))
                    )
                )
        }
    } catch (e: Exception) {
        e.printStackTrace()
        ItemStackBuilder.of(Material.BARRIER)
            .name(Component.translatable("pylon.pyloncore.guide.button.fluid.error"))
    }

    override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
        try {
            if (clickType.isLeftClick) {
                val page = FluidRecipesPage(currentFluid.key)
                if (page.pages.isNotEmpty()) {
                    page.open(player)
                }
            } else {
                val page = FluidUsagesPage(currentFluid)
                if (page.pages.isNotEmpty()) {
                    page.open(player)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
