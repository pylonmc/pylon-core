package io.github.pylonmc.pylon.core.util.gui

import io.github.pylonmc.pylon.core.i18n.PylonArgument
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.util.gui.unit.UnitFormat
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.TooltipDisplay
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AbstractItem
import java.time.Duration

/**
 * An item that counts down (by default) from a maximum durability, symbolising progress.
 *
 * For example, this might be used in a custom furnace to show how much burn time is left on
 * the current fuel. In this case, you'd want override [totalTime] and set it to the the total
 * burn time of the fuel (eg: 80 seconds for coal), and then gradually increase [progress] as the
 * remaining burn time decreased.
 *
 * @param material The material to use for the item
 * @param inverse If true, the progress bar will be inverted, meaning that 0.0 is full and 1.0 is empty.
 */
abstract class ProgressItem @JvmOverloads constructor(
    private val material: Material,
    private val inverse: Boolean = false
) : AbstractItem() {

    var progress: Double = 0.0
        set(value) {
            field = value.coerceIn(0.0, 1.0)
            notifyWindows()
        }

    open val totalTime: Duration? = null

    @Suppress("UnstableApiUsage")
    override fun getItemProvider(): ItemProvider {
        var progressValue = progress
        if (!inverse) {
            progressValue = 1 - progressValue
        }
        val builder = ItemStackBuilder.of(material)
            .set(DataComponentTypes.MAX_STACK_SIZE, 1)
            .set(DataComponentTypes.MAX_DAMAGE, MAX_DURABILITY)
            .set(DataComponentTypes.DAMAGE, (progressValue * MAX_DURABILITY).toInt())
            .set(
                DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay()
                    .addHiddenComponents(DataComponentTypes.DAMAGE, DataComponentTypes.MAX_DAMAGE)
            )
        totalTime?.let {
            val remaining = it - it * progress
            builder.lore(
                Component.translatable(
                    "pylon.pyloncore.gui.time_left",
                    PylonArgument.of("time", UnitFormat.formatDuration(remaining))
                )
            )
        }
        completeItem(builder)
        return builder
    }

    /**
     * This can be used to modify the item being displayed.
     *
     * @param builder The item builder representing the progress item
     */
    protected abstract fun completeItem(builder: ItemStackBuilder)

    override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {}

    companion object {
        private const val MAX_DURABILITY = 1000
    }
}

private operator fun Duration.times(value: Double): Duration = Duration.ofMillis((toMillis() * value).toLong())