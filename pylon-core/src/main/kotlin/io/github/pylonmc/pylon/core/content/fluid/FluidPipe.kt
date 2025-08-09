package io.github.pylonmc.pylon.core.content.fluid

import io.github.pylonmc.pylon.core.block.BlockStorage
import io.github.pylonmc.pylon.core.entity.EntityStorage
import io.github.pylonmc.pylon.core.fluid.FluidManager
import io.github.pylonmc.pylon.core.fluid.PylonFluid
import io.github.pylonmc.pylon.core.fluid.connecting.*
import io.github.pylonmc.pylon.core.fluid.tags.FluidTemperature
import io.github.pylonmc.pylon.core.i18n.PylonArgument
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.item.base.PylonInteractor
import io.github.pylonmc.pylon.core.item.base.PylonItemEntityInteractor
import io.github.pylonmc.pylon.core.util.gui.unit.UnitFormat
import io.github.pylonmc.pylon.core.util.position.BlockPosition
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import java.util.function.Predicate

open class FluidPipe(stack: ItemStack) : PylonItem(stack), PylonItemEntityInteractor, PylonInteractor {
    val material = Material.valueOf(getSettings().getOrThrow<String>("material").uppercase())
    val fluidPerSecond = getSettings().getOrThrow<Double>("fluid-per-second")
    val allowedTemperatures = getSettings().getOrThrow<List<String>>("allowed-temperatures")
        .map { s -> FluidTemperature.valueOf(s.uppercase()) }

    override fun getPlaceholders(): List<PylonArgument> = listOf(
        PylonArgument.of("fluid_per_second", UnitFormat.MILLIBUCKETS_PER_SECOND.format(fluidPerSecond)),
        PylonArgument.of(
            "temperatures", Component.join(
                JoinConfiguration.separator(Component.text(", ")),
                allowedTemperatures.map(FluidTemperature::valueText)
            )
        )
    )

    open fun canPass(fluid: PylonFluid): Boolean {
        return fluid.hasTag<FluidTemperature>() && fluid.getTag<FluidTemperature>() in allowedTemperatures
    }

    override fun onUsedToRightClickEntity(event: PlayerInteractEntityEvent) {
        if (event.hand != EquipmentSlot.HAND) {
            return
        }

        val interaction = EntityStorage.get(event.rightClicked)
        if (interaction is FluidPointInteraction) {
            if (!ConnectingService.isConnecting(event.getPlayer())) {
                ConnectingService.startConnection(event.getPlayer(), ConnectingPointInteraction(interaction), this)
                return
            }
        }

        if (ConnectingService.isConnecting(event.player)) {
            val segment = ConnectingService.placeConnection(event.player)
            if (segment != null) {
                FluidManager.setFluidPerSecond(segment, fluidPerSecond)
                FluidManager.setFluidPredicate(segment, this::canPass)
            }
        }
    }

    override fun onUsedToRightClick(event: PlayerInteractEvent) {
        if (event.hand != EquipmentSlot.HAND) {
            return
        }

        val action = event.action
        val block: Block? = event.clickedBlock
        val player: Player = event.player

        if (block != null && action == Action.RIGHT_CLICK_BLOCK && !ConnectingService.isConnecting(player)) {
            if (!tryStartConnection(player, block)) {
                tryStartConnection(player, block.getRelative(event.blockFace))
            }
        }

        if ((action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) && ConnectingService.isConnecting(
                player
            )
        ) {
            val segment = ConnectingService.placeConnection(player)
            if (segment != null) {
                FluidManager.setFluidPerSecond(segment, fluidPerSecond)
                FluidManager.setFluidPredicate(segment, this::canPass)
            }
        }
    }

    private fun tryStartConnection(player: Player, block: Block): Boolean {
        val pylonBlock = BlockStorage.get(block)
        if (pylonBlock is FluidPipeConnector) {
            if (pylonBlock.pipe != this) {
                player.sendActionBar(Component.translatable("pylon.pylonbase.message.pipe.not_of_same_type"))
                return true
            }
            val connectingPoint = ConnectingPointPipeConnector(pylonBlock)
            ConnectingService.startConnection(player, connectingPoint, this)
            return true
        }

        if (pylonBlock is FluidPipeMarker) {
            val pipeDisplay = pylonBlock.getPipeDisplay()!!
            if (pipeDisplay.pipe != this) {
                player.sendActionBar(Component.translatable("pylon.pylonbase.message.pipe.not_of_same_type"))
                return true
            }
            val connectingPoint = ConnectingPointPipeMarker(pylonBlock)
            ConnectingService.startConnection(player, connectingPoint, this)
            return true
        }

        if (block.type.isAir()) {
            val connectingPoint = ConnectingPointNewBlock(BlockPosition(block))
            ConnectingService.startConnection(player, connectingPoint, this)
            return true
        }

        return false
    }
}