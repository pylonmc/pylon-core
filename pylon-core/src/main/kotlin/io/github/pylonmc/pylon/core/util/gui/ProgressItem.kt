package io.github.pylonmc.pylon.core.util.gui

import io.github.pylonmc.pylon.core.i18n.PylonArgument
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.util.gui.unit.UnitFormat
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.Style
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AbstractItem
import java.time.Duration

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
        totalTime?.let {
            val remaining = it - it * progress

            var component = Component.text()
            val days = remaining.toDaysPart()
            if (days > 0) {
                component = component.append(
                    UnitFormat.DAYS.format(days)
                        .abbreviate(false)
                        .unitStyle(Style.empty())
                )
            }
            val hours = remaining.toHoursPart()
            if (hours > 0) {
                component = component.append(
                    UnitFormat.HOURS.format(hours)
                        .abbreviate(false)
                        .unitStyle(Style.empty())
                )
            }
            val minutes = remaining.toMinutesPart()
            if (minutes > 0) {
                component = component.append(
                    UnitFormat.MINUTES.format(minutes)
                        .abbreviate(false)
                        .unitStyle(Style.empty())
                )
            }
            val seconds = remaining.toSecondsPart()
            component = component.append(
                UnitFormat.SECONDS.format(seconds)
                    .abbreviate(false)
                    .unitStyle(Style.empty())
            )
            builder.lore(
                Component.translatable(
                    "pylon.pyloncore.gui.time_left",
                    PylonArgument.of("time", component.build())
                )
            )
        }
        completeItem(builder)
        return builder
    }

    protected abstract fun completeItem(builder: ItemStackBuilder)

    override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {}

    companion object {
        private const val MAX_DURABILITY = 1000
    }
}

private operator fun Duration.times(value: Double): Duration = Duration.ofMillis((toMillis() * value).toLong())