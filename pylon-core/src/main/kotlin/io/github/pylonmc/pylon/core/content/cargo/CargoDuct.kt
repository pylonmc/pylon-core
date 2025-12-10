package io.github.pylonmc.pylon.core.content.cargo

import io.github.pylonmc.pylon.core.block.BlockStorage
import io.github.pylonmc.pylon.core.block.PylonBlock
import io.github.pylonmc.pylon.core.block.base.PylonBreakHandler
import io.github.pylonmc.pylon.core.block.base.PylonCargoBlock
import io.github.pylonmc.pylon.core.block.base.PylonEntityHolderBlock
import io.github.pylonmc.pylon.core.block.base.PylonEntityHolderBlock.Companion.holders
import io.github.pylonmc.pylon.core.block.context.BlockBreakContext
import io.github.pylonmc.pylon.core.block.context.BlockCreateContext
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.entity.display.ItemDisplayBuilder
import io.github.pylonmc.pylon.core.entity.display.transform.LineBuilder
import io.github.pylonmc.pylon.core.entity.display.transform.TransformBuilder
import io.github.pylonmc.pylon.core.event.PylonCargoConnectEvent
import io.github.pylonmc.pylon.core.event.PylonCargoDuctDisconnectEvent
import io.github.pylonmc.pylon.core.util.IMMEDIATE_FACES
import io.github.pylonmc.pylon.core.util.position.BlockPosition
import io.github.pylonmc.pylon.core.util.position.position
import io.github.pylonmc.pylon.core.util.pylonKey
import io.github.pylonmc.pylon.core.util.setNullable
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityRemoveEvent
import org.bukkit.persistence.PersistentDataContainer

class CargoDuct : PylonBlock, PylonBreakHandler, PylonEntityHolderBlock {

    var connectedFaces = mutableListOf<BlockFace>()

    @Suppress("unused")
    constructor(block: Block, context: BlockCreateContext) : super(block) {
        updateConnectedFaces()
    }

    @Suppress("unused")
    constructor(block: Block, pdc: PersistentDataContainer) : super(block) {
        connectedFaces = pdc.get(connectedFacesKey, connectedFacesType)!!.toMutableList()
    }

    override fun write(pdc: PersistentDataContainer) {
        pdc.setNullable(connectedFacesKey, connectedFacesType, connectedFaces)
    }

    override fun postBreak(context: BlockBreakContext) {
        for (face in connectedFaces) {
            val connectedBlock = connectedBlock(face)
            when (connectedBlock) {
                is CargoDuct -> {
                    connectedBlock.connectedFaces.remove(face.oppositeFace)
                    connectedBlock.updateConnectedFaces()
                    PylonCargoDuctDisconnectEvent(this, connectedBlock).callEvent()
                }
                is PylonCargoBlock -> {
                    PylonCargoDuctDisconnectEvent(this, connectedBlock).callEvent()
                }
            }
        }
    }

    fun updateConnectedFaces() {
        if (connectedFaces.size == 2) {
            return
        }

        val adjacentCargoBlocks = mutableMapOf<BlockFace, PylonBlock>()
        for (face in IMMEDIATE_FACES) {
            val adjacentBlock = BlockStorage.get(block.getRelative(face))
            if (face !in connectedFaces && (adjacentBlock is CargoDuct || adjacentBlock is PylonCargoBlock)) {
                adjacentCargoBlocks.put(face, adjacentBlock)
            }
        }

        // 1: Prioritise PylonCargoBlocks
        for ((face, block) in adjacentCargoBlocks) {
            if (connectedFaces.size != 2 && block is PylonCargoBlock && block.cargoLogisticGroups.containsKey(face.oppositeFace)) {
                if (PylonCargoConnectEvent(this, block).callEvent()) {
                    connectedFaces.add(face)
                }
            }
        }

        // 2: Prioritise PylonDucts which already have a connection
        for ((face, block) in adjacentCargoBlocks) {
            if (connectedFaces.size != 2 && block is CargoDuct && block.connectedFaces.size == 1) {
                if (PylonCargoConnectEvent(this, block).callEvent()) {
                    connectedFaces.add(face)
                    block.connectedFaces.add(face.oppositeFace)
                }
            }
        }

        // 3: Prioritise PylonDucts without connections
        for ((face, block) in adjacentCargoBlocks) {
            if (connectedFaces.size != 2 && block is CargoDuct && block.connectedFaces.isEmpty()) {
                if (PylonCargoConnectEvent(this, block).callEvent()) {
                    connectedFaces.add(face)
                    block.connectedFaces.add(face.oppositeFace)
                }
            }
        }

        updateDisplays()
    }

    fun updateDisplays() {
        // Delete any existing, outdated displays (either a single 'not connected' cube display or a
        // display that continues the same direction as any of the connected faces)
        for (face in connectedFaces) {
            (connectedBlock(face) as? CargoDuct)
                ?.getHeldEntity(ductDisplayName(face))
                ?.remove()
            (connectedBlock(face) as? CargoDuct)
                ?.getHeldEntity(NOT_CONNECTED_DUCT_DISPLAY_NAME)
                ?.remove()
        }
        for (entity in heldEntities.keys.toList()) { // clone to prevent concurrent modification exception
            getHeldEntity(entity)?.remove()
        }

        // For performance reasons, if we can use one display entity instead of
        // several, we always should. We do this by deleting any existing entities
        // on the same axis and then spawning a new display entity for the next
        // duct, and a new display entity for the previous duct. These two entities
        // span the entire line from this duct to the end of the next and previous
        // lines

        // Case 1: Duct has no connected blocks
        if (connectedFaces.isEmpty()) {
            // Spawn a cube display
            createNotConnectedDuctDisplay(block.location.toCenterLocation())
        }

        // Case 2: Duct has two connected blocks on opposite sides, forming a line
        else if (connectedFaces.size == 2 && connectedFaces[0] == connectedFaces[1].oppositeFace) {
            // Spawn a display spanning the entire line
            val endpoint0 = findEndOfLine(connectedFaces[0])
            val endpoint1 = findEndOfLine(connectedFaces[1])
            createDuctDisplay(endpoint0, endpoint1, connectedFaces[0].oppositeFace)
        }

        // Case 3: Duct has either one or two connected blocks, and if two blocks are
        // connected, they do not form a line across this block (this is handled in
        // case 2)
        else {
            // Spawn a display to each of the two connected blocks
            createDuctDisplay(findEndOfLine(connectedFaces[0]), this.block, connectedFaces[0].oppositeFace)
            if (connectedFaces.size == 2) {
                createDuctDisplay(findEndOfLine(connectedFaces[1]), this.block, connectedFaces[1].oppositeFace)
            }
        }
    }

    /**
     * Recursively traverses the next face only if it is the provided face.
     *
     * This has the effect of traversing to the end of the line whose direction
     * is provided by the current block and the next block.
     */
    private fun findEndOfLine(face: BlockFace): Block {
        var currentDuct = this
        while (true) {
            val nextBlock = currentDuct.connectedBlock(face)
            if (nextBlock is CargoDuct) {
                currentDuct = nextBlock
                continue
            }

            if (nextBlock is PylonCargoBlock) {
                return nextBlock.block
            }

            if (nextBlock == null) {
                return currentDuct.block
            }
        }
    }

    private fun connectedBlock(face: BlockFace): PylonBlock? {
        if (face !in connectedFaces) {
            return null
        }
        return BlockStorage.get(block.getRelative(face))
    }

    private fun createDuctDisplay(from: Block, to: Block, fromToFace: BlockFace) {
        // Need to do some detective work to find out the correct thickness. The rule
        // is that the thickness of the display connecting [from] and [to] must be
        // different to the thickness of the existing display on [from] and [to] (if
        // they exist). Note there can only be one other existing display considering
        // we're in the process of making a new connection to the duct here.
        val availableThicknesses = thicknesses.toMutableList()
        val fromDuct = if (from == this.block) {
            this // Special case: This block is not in BlockStorage yet
        } else {
            BlockStorage.getAs<CargoDuct>(from)
        }
        fromDuct?.heldEntities?.keys?.forEach { name ->
            fromDuct.getHeldEntity(name)?.persistentDataContainer?.get(thicknessKey, thicknessType).let { thickness ->
                availableThicknesses.remove(thickness)
            }
        }
        val toDuct = if (this.block == to) {
            this // Special case: This block is not in BlockStorage yet
        } else {
            BlockStorage.getAs<CargoDuct>(to)
        }
        toDuct?.heldEntities?.keys?.forEach { name ->
            toDuct.getHeldEntity(name)?.persistentDataContainer?.get(thicknessKey, thicknessType)?.let { thickness ->
                availableThicknesses.remove(thickness)
            }
        }
        val thickness = availableThicknesses[0]

        // Now to actually build the display
        // It's possible one of the displays will be a PylonCargoBlock, in which case it could be a solid block
        // This would occlude the display entity and cause it to render with brightness 0
        // To avoid this, we'll just spawn the entity at this duct, since we know it's a duct (and therefore a
        // structure void, which will not occlude the display entity)
        var spawnLocation = this.block.location.toCenterLocation()
        val display = ItemDisplayBuilder()
            .transformation(LineBuilder()
                .from(from.location.toCenterLocation().subtract(spawnLocation).toVector().toVector3d())
                .to(to.location.toCenterLocation().subtract(spawnLocation).toVector().toVector3d())
                .thickness(thickness)
                .extraLength(thickness)
                .build()
            )
            .material(Material.GRAY_CONCRETE)
            .build(spawnLocation)
        display.persistentDataContainer.set(thicknessKey, thicknessType, thickness)

        // Add the display to every CargoDuct on the line
        val associatedBlocks = mutableListOf<BlockPosition>()
        // (start)
        BlockStorage.getAs<CargoDuct>(from)?.addEntity(ductDisplayName(fromToFace), display)
        if (from == this.block) {
            // Special case: This block is not in BlockStorage yet so above code will not work
            addEntity(ductDisplayName(fromToFace), display)
        }
        associatedBlocks.add(from.position)
        // (middle)
        var current = from
        while (true) {
            current = current.getRelative(fromToFace)
            if (current == to) {
                break
            }
            BlockStorage.getAs<CargoDuct>(current)?.let {
                it.addEntity(ductDisplayName(fromToFace), display)
                it.addEntity(ductDisplayName(fromToFace.oppositeFace), display)
            }
            if (current == this.block) {
                // Special case: This block is not in BlockStorage yet so above code will not work
                addEntity(ductDisplayName(fromToFace), display)
                addEntity(ductDisplayName(fromToFace.oppositeFace), display)
            }
            associatedBlocks.add(current.position)
        }
        // (end)
        BlockStorage.getAs<CargoDuct>(to)?.addEntity(ductDisplayName(fromToFace.oppositeFace), display)
        if (to == this.block) {
            // Special case: This block is not in BlockStorage yet so above code will not work
            addEntity(ductDisplayName(fromToFace.oppositeFace), display)
        }
        associatedBlocks.add(to.position)

        // Also add the blocks to the display's PDC (see onEntityRemove in companion for explanation)
        display.persistentDataContainer.set(blocksKey, blocksType, associatedBlocks)
    }

    private fun createNotConnectedDuctDisplay(center: Location) {
        val display = ItemDisplayBuilder()
            .transformation(TransformBuilder()
                .scale(thicknesses[0])
            )
            .material(Material.GRAY_CONCRETE)
            .build(center)

        addEntity(NOT_CONNECTED_DUCT_DISPLAY_NAME, display)
    }

    companion object : Listener {
        const val NOT_CONNECTED_DUCT_DISPLAY_NAME = "duct-item-display:not-connected"

        // Q: 'Why the hell are there 3 different thicknesses?'
        // A: To prevent Z-fighting. It's expected that blocks trying to create a seamless connection
        // to cargo ducts will use thickness 0.35 hence why it isn't used here
        val thicknesses = mutableListOf(0.3495F, 0.3490F, 0.3485F)

        val thicknessKey = pylonKey("thickness")
        val thicknessType = PylonSerializers.FLOAT

        val connectedFacesKey = pylonKey("connected_faces")
        val connectedFacesType = PylonSerializers.LIST.listTypeFrom(PylonSerializers.BLOCK_FACE)

        val blocksKey = pylonKey("blocks")
        val blocksType = PylonSerializers.LIST.listTypeFrom(PylonSerializers.BLOCK_POSITION)

        fun ductDisplayName(face: BlockFace) = "duct-item-display:${face.name}"

        /**
         * Cargo duct displays are 'owned' by multiple blocks, but the entity removal
         * handling in [PylonEntityHolderBlock] assumes a single block holds the
         * entity. We therefore have to roll our own entity removal logic that will
         * store *all* the blocks that own the entity in the entity's PDC, and remove
         * the entity from all of those blocks when it is removed.
         */
        @EventHandler
        private fun onEntityRemove(event: EntityRemoveEvent) {
            if (event.cause == EntityRemoveEvent.Cause.UNLOAD || event.cause == EntityRemoveEvent.Cause.PLAYER_QUIT) return
            val blockPositions = event.entity.persistentDataContainer.get(blocksKey, blocksType) ?: return
            for (blockPos in blockPositions) {
                val block = BlockStorage.get(blockPos) as? PylonEntityHolderBlock ?: continue
                holders[block]?.entries?.removeIf { it.value == event.entity.uniqueId }
            }
        }
    }
}
