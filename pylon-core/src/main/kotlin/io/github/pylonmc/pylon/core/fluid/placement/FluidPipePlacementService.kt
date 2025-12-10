package io.github.pylonmc.pylon.core.fluid.placement

import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.block.BlockStorage
import io.github.pylonmc.pylon.core.content.fluid.*
import io.github.pylonmc.pylon.core.entity.EntityStorage
import io.github.pylonmc.pylon.core.fluid.FluidManager
import io.github.pylonmc.pylon.core.fluid.placement.FluidPipePlacementTask.Companion.pipesUsed
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.util.blocksOnPath
import io.github.pylonmc.pylon.core.util.position.ChunkPosition
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.event.world.ChunkUnloadEvent
import org.bukkit.inventory.EquipmentSlot
import java.util.UUID

internal object FluidPipePlacementService : Listener {
    /**
     * Maps player doing the connection to the origin of the connection
     */
    private val connectionsInProgress = mutableMapOf<Player, FluidPipePlacementTask>()

    /**
     * ANNOYING HACK ALERT: Imagine placing a pipe on a block, creating a new intersection. You look
     * at the spot where the new intersection is created. Well, guess what, A DUPLICATE PLAYER INTERACT
     * EVENT IS FIRED FOR THAT WITH THE NEW STRUCTURE VOID WE JUST CREATED FOR THE INTERSECTION. This
     * causes the player to start placing another pipe. This set exists to prevent that from happening
     * by forcing a 1-tick delay between one pipe placement ending and the next one starting.
     */
    private val playersWhoFinishedConnectionInLastTick = mutableSetOf<Player>()

    fun startConnection(player: Player, startPoint: FluidPipePlacementPoint, pipe: FluidPipe) {
        check(!connectionsInProgress.containsKey(player))
        // Clone to prevent the PylonItem instance being shared between player's hotbar and the pipe itself
        connectionsInProgress.put(player, FluidPipePlacementTask(player, startPoint, FluidPipe(pipe.stack.clone())))
    }

    @JvmStatic
    fun cancelConnection(player: Player) {
        connectionsInProgress.remove(player)!!.cancel()
    }

    fun placeConnection(player: Player): UUID? {
        check(isConnecting(player)) { "Player is not currently connecting anything" }

        // try to finish connecting task
        val result = connectionsInProgress[player]!!.finish() ?: return null
        connectionsInProgress.remove(player)
        playersWhoFinishedConnectionInLastTick.add(player)
        Bukkit.getScheduler().runTaskLater(PylonCore, Runnable {
            playersWhoFinishedConnectionInLastTick.remove(player)
        }, 1)

        // give player pipes
        if (player.gameMode != GameMode.CREATIVE) {
            player.inventory.getItem(EquipmentSlot.HAND).subtract(result.pipesUsed)
        }

        return result.to.point.segment
    }

    fun isConnecting(player: Player): Boolean
        = connectionsInProgress.containsKey(player)

    fun connectedLastTick(player: Player): Boolean
            = playersWhoFinishedConnectionInLastTick.contains(player)

    fun cleanup() {
        for (player in connectionsInProgress.keys) {
            cancelConnection(player)
        }
    }

    private fun taskContainsBlocksInChunk(task: FluidPipePlacementTask, chunk: ChunkPosition): Boolean {
        return task.origin.position.chunk == chunk || task.target.position.chunk == chunk
    }

    fun connect(
        from: FluidPipePlacementPoint,
        to: FluidPipePlacementPoint,
        pipe: FluidPipe
    ): FluidPipeDisplay {
        // Get start and end point
        val fromDisplay = from.create()
        val toDisplay = to.create()

        // Create fluid pipe display
        val pipeAmount = pipesUsed(from.position, to.position)
        val pipeDisplay = FluidPipeDisplay(pipe, pipeAmount, fromDisplay, toDisplay)

        // Add the fluid pipe display to the from/to displays as an entity
        fromDisplay.connectPipeDisplay(pipeDisplay.uuid)
        toDisplay.connectPipeDisplay(pipeDisplay.uuid)

        // Turn all the blocks the pipe spans into marker blocks
        for (block in blocksOnPath(from.position, to.position)) {
            val marker = BlockStorage.placeBlock(block, FluidSectionMarker.KEY) as FluidSectionMarker
            marker.addEntity("pipe", pipeDisplay)
        }

        // Actually connect the new points we created
        FluidManager.connect(fromDisplay.point, toDisplay.point)

        return pipeDisplay
    }

    fun disconnect(
        from: FluidPointDisplay,
        to: FluidPointDisplay,
        removeEmptyConnectors: Boolean
    ) {
        // Find the pipe display; it will be the only common display between the two points
        val pipeDisplaySet = from.connectedPipeDisplays.toMutableSet()
        pipeDisplaySet.retainAll(to.connectedPipeDisplays)
        check(pipeDisplaySet.size == 1)
        val pipeDisplayUuid = pipeDisplaySet.iterator().next()

        // Yeet the pipe display itself
        val pipeDisplay = EntityStorage.getAs<FluidPipeDisplay>(pipeDisplayUuid)
        check(pipeDisplay != null)
        pipeDisplay.entity.remove()

        // Disconnect the points
        FluidManager.disconnect(from.point, to.point)

        // Remove markers
        for (block in blocksOnPath(from.point.position, to.point.position)) {
            BlockStorage.breakBlock(block)
        }

        // Remove the deleted pipe from interactions
        from.disconnectPipeDisplay(pipeDisplay.uuid)
        to.disconnectPipeDisplay(pipeDisplay.uuid)

        // Delete connectors if they're now empty
        val fromConnector = BlockStorage.get(from.point.position)
        if (fromConnector is FluidIntersectionMarker && removeEmptyConnectors && from.connectedPipeDisplays.isEmpty()) {
            BlockStorage.breakBlock(fromConnector.block)
        }
        val toConnector = BlockStorage.get(to.point.position)
        if (toConnector is FluidIntersectionMarker && removeEmptyConnectors && to.connectedPipeDisplays.isEmpty()) {
            BlockStorage.breakBlock(toConnector.block)
        }
    }

    @EventHandler
    private fun onPlayerQuit(event: PlayerQuitEvent) {
        if (connectionsInProgress.containsKey(event.getPlayer())) {
            cancelConnection(event.getPlayer())
        }
    }

    @EventHandler
    private fun onPlayerScroll(event: PlayerItemHeldEvent) {
        val heldItem = event.getPlayer().inventory.getItem(event.previousSlot)
        if (PylonItem.fromStack(heldItem) is FluidPipe && connectionsInProgress.containsKey(event.getPlayer())) {
            cancelConnection(event.getPlayer())
        }
    }

    @EventHandler
    private fun onSwap(e: PlayerSwapHandItemsEvent) {
        val mainHandPylon = PylonItem.fromStack(e.mainHandItem)
        if (mainHandPylon != null && mainHandPylon is FluidPipe) {
            e.isCancelled = true
            return
        }

        val otherHandPylon = PylonItem.fromStack(e.offHandItem)
        if (otherHandPylon != null && otherHandPylon is FluidPipe) {
            e.isCancelled = true
            return
        }
    }

    /**
     * Intended to prevent issues if players teleport away while placing a pipe
     */
    @EventHandler
    private fun onChunkUnload(event: ChunkUnloadEvent) {
        val toRemove = connectionsInProgress.values.filter { task: FluidPipePlacementTask ->
            taskContainsBlocksInChunk(task, ChunkPosition(event.getChunk()))
        }
        for (task in toRemove) {
            task.cancel()
        }
        connectionsInProgress.values.removeAll(toRemove)
    }
}
