package io.github.pylonmc.pylon.core.fluid.connecting

import io.github.pylonmc.pylon.core.block.BlockStorage
import io.github.pylonmc.pylon.core.content.fluid.*
import io.github.pylonmc.pylon.core.entity.EntityStorage
import io.github.pylonmc.pylon.core.fluid.FluidManager
import io.github.pylonmc.pylon.core.fluid.connecting.ConnectingTask.Companion.blocksOnPath
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.util.position.ChunkPosition
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.world.ChunkUnloadEvent
import org.bukkit.inventory.EquipmentSlot

object ConnectingService : org.bukkit.event.Listener {
    // Maps player doing the connection to the origin of the connection
    private val connectionsInProgress = mutableMapOf<Player, ConnectingTask>()

    fun startConnection(player: Player, startPoint: ConnectingPoint, pipe: FluidPipe) {
        check(!ConnectingService.connectionsInProgress.containsKey(player))
        ConnectingService.connectionsInProgress.put(player, ConnectingTask(player, startPoint, pipe))
    }

    @JvmStatic
    fun cancelConnection(player: Player) {
        ConnectingService.connectionsInProgress.remove(player)!!.cancel()
    }

    fun placeConnection(player: Player): java.util.UUID? {
        val connectingTask = ConnectingService.connectionsInProgress[player]!!
        val result = connectingTask.finish() ?: return null

        if (player.gameMode != GameMode.CREATIVE) {
            player.inventory.getItem(EquipmentSlot.HAND).subtract(result.pipesUsed)
        }

        ConnectingService.connectionsInProgress.remove(player)

        val pylonItem = PylonItem.fromStack(player.inventory.getItem(EquipmentSlot.HAND))
        if (result.to.face == null && pylonItem is FluidPipe) {
            // start new connection from the point we just placed if it didn't have a face
            // if it does have a face, we can't go any further so don't bother starting a new connection
            val connectingPoint = ConnectingPointInteraction(result.to)
            ConnectingService.connectionsInProgress.put(player, ConnectingTask(player, connectingPoint, connectingTask.pipe))
        }

        return result.to.point.segment
    }

    fun isConnecting(player: Player): Boolean
        = ConnectingService.connectionsInProgress.containsKey(player)

    fun cleanup() {
        for (player in ConnectingService.connectionsInProgress.keys) {
            ConnectingService.cancelConnection(player)
        }
    }

    private fun taskContainsBlocksInChunk(task: ConnectingTask, chunk: ChunkPosition): Boolean {
        return task.from.position.chunk == chunk || task.to.position.chunk == chunk
    }

    fun connect(
        from: ConnectingPoint,
        to: ConnectingPoint,
        pipe: FluidPipe
    ): FluidPipeDisplay {
        val originInteraction: FluidPointInteraction = from.create()
        val targetInteraction: FluidPointInteraction = to.create()

        val pipeAmount = ConnectingTask.pipesUsed(from.position, to.position)

        val pipeDisplay: FluidPipeDisplay =
            FluidPipeDisplay.make(pipe, pipeAmount, originInteraction, targetInteraction)

        originInteraction.connectedPipeDisplays.add(pipeDisplay.uuid)
        targetInteraction.connectedPipeDisplays.add(pipeDisplay.uuid)

        for (block in blocksOnPath(from.position, to.position)) {
            val marker = BlockStorage.placeBlock(block, FluidPipeMarker.KEY) as FluidPipeMarker
            marker.pipeDisplay = pipeDisplay.uuid
            marker.from = originInteraction.uuid
            marker.to = targetInteraction.uuid
        }

        FluidManager.connect(originInteraction.point, targetInteraction.point)

        return pipeDisplay
    }

    fun disconnect(
        from: FluidPointInteraction,
        to: FluidPointInteraction,
        removeEmptyConnectors: kotlin.Boolean
    ) {
        // the pipe display will be the only common display between the two points
        val pipeDisplaySet = from.connectedPipeDisplays.toMutableSet()
        pipeDisplaySet.retainAll(to.connectedPipeDisplays)
        check(pipeDisplaySet.size == 1)

        val pipeDisplay = EntityStorage.getAs<FluidPipeDisplay>(pipeDisplaySet.iterator().next())
        check(pipeDisplay != null)

        pipeDisplay.entity.remove()

        FluidManager.disconnect(from.point, to.point)

        // remove markers
        for (block in blocksOnPath(from.point.position, to.point.position)) {
            BlockStorage.breakBlock(block)
        }

        // remove the deleted pipe from interactions
        from.connectedPipeDisplays.remove(pipeDisplay.uuid)
        to.connectedPipeDisplays.remove(pipeDisplay.uuid)

        // delete connectors if they're now empty
        val fromConnector = BlockStorage.get(from.point.position)
        if (fromConnector is FluidPipeConnector && removeEmptyConnectors && from.connectedPipeDisplays.isEmpty()) {
            BlockStorage.breakBlock(fromConnector.block)
        }
        val toConnector = BlockStorage.get(to.point.position)
        if (toConnector is FluidPipeConnector && removeEmptyConnectors && to.connectedPipeDisplays.isEmpty()) {
            BlockStorage.breakBlock(toConnector.block)
        }
    }

    @EventHandler
    private fun onPlayerQuit(event: PlayerQuitEvent) {
        if (ConnectingService.connectionsInProgress.containsKey(event.getPlayer())) {
            ConnectingService.cancelConnection(event.getPlayer())
        }
    }

    @EventHandler
    private fun onPlayerScroll(event: PlayerItemHeldEvent) {
        val heldItem = event.getPlayer().inventory.getItem(event.previousSlot)
        if (PylonItem.fromStack(heldItem) is FluidPipe && ConnectingService.connectionsInProgress.containsKey(event.getPlayer())) {
            ConnectingService.cancelConnection(event.getPlayer())
        }
    }

    /**
     * Intended to prevent issues if players teleport away while placing a pipe
     */
    @EventHandler
    private fun onChunkUnload(event: ChunkUnloadEvent) {
        val toRemove = ConnectingService.connectionsInProgress.values.filter { task: ConnectingTask ->
            ConnectingService.taskContainsBlocksInChunk(task, ChunkPosition(event.getChunk()))
        }
        for (task in toRemove) {
            task.cancel()
        }
        ConnectingService.connectionsInProgress.values.removeAll(toRemove)
    }
}
