package io.github.pylonmc.pylon.core.fluid.connecting

import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.block.BlockStorage
import io.github.pylonmc.pylon.core.config.PylonConfig
import io.github.pylonmc.pylon.core.content.fluid.FluidPipe
import io.github.pylonmc.pylon.core.content.fluid.FluidPipeConnector
import io.github.pylonmc.pylon.core.content.fluid.FluidPipeMarker
import io.github.pylonmc.pylon.core.content.fluid.FluidPointInteraction
import io.github.pylonmc.pylon.core.entity.EntityStorage
import io.github.pylonmc.pylon.core.entity.display.ItemDisplayBuilder
import io.github.pylonmc.pylon.core.entity.display.transform.LineBuilder
import io.github.pylonmc.pylon.core.entity.display.transform.TransformBuilder
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.util.position.BlockPosition
import io.github.pylonmc.pylon.core.util.vectorToBlockFace
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import org.joml.Matrix3f
import org.joml.RoundingMode
import org.joml.Vector3f
import org.joml.Vector3i
import kotlin.math.roundToInt
import kotlin.math.roundToLong

class ConnectingTask(
    val player: Player,
    val from: ConnectingPoint,
    val pipe: FluidPipe
) {
    class Result(val to: FluidPointInteraction, val pipesUsed: Int)

    private val display = ItemDisplayBuilder()
        .material(Material.WHITE_CONCRETE)
        .brightness(15) // transformation will be set later, make the block invisible now to prevent flash of white on place
        .transformation(TransformBuilder().scale(0f))
        .build(from.position.location.toCenterLocation())

    var to = from
        private set

    var isValid = false
        private set

    val task = Bukkit.getScheduler().runTaskTimer(
        PylonCore,
        Runnable { tick(true) },
        0,
        PylonConfig.pipePlacementTaskIntervalTicks
    )

    private fun tick(interpolate: Boolean) {
        val pylonItem = PylonItem.fromStack(player.inventory.getItem(EquipmentSlot.HAND))
        if (pylonItem !is FluidPipe) {
            ConnectingService.cancelConnection(player)
            return
        }

        if (!from.isStillValid || !to.isStillValid) {
            ConnectingService.cancelConnection(player)
            return
        }

        val previousToPosition = to.position

        recalculateTo()

        if (player.gameMode != GameMode.CREATIVE
            && pipesUsed(from.position, to.position) > player.inventory.getItem(EquipmentSlot.HAND).amount
        ) {
            isValid = false
            player.sendActionBar(Component.translatable("pylon.pylonbase.message.pipe.not_enough_pipes"))
            display.setItemStack(ItemStack(Material.RED_CONCRETE))
        } else if (!this.isPipeTypeValid) {
            isValid = false
            player.sendActionBar(Component.translatable("pylon.pylonbase.message.pipe.not_of_same_type"))
            display.setItemStack(ItemStack(Material.RED_CONCRETE))
        } else if (!this.isPlacementValid) {
            isValid = false
            player.sendActionBar(Component.translatable("pylon.pylonbase.message.pipe.cannot_place_here"))
            display.setItemStack(ItemStack(Material.RED_CONCRETE))
        } else {
            isValid = true
            player.sendActionBar(Component.translatable("pylon.pylonbase.message.pipe.connecting"))
            display.setItemStack(ItemStack(Material.WHITE_CONCRETE))
        }

        val targetOffset = to.position.location.toVector().toVector3f()
            .add(to.offset)
            .sub(from.position.location.toVector().toVector3f())

        val difference = to.position.vector3i.sub(previousToPosition.vector3i)

        display.setTransformationMatrix(
            LineBuilder()
                .from(from.offset)
                .to(targetOffset)
                .thickness(0.101) // not 0.1 to prevent z-fighting with existing pipes
                .build().buildForItemDisplay()
        )

        if (isCardinalDirection(difference) && interpolate) {
            display.interpolationDelay = 0
            display.interpolationDuration = PylonConfig.pipePlacementTaskIntervalTicks.toInt()
        }
    }

    fun cancel() {
        task.cancel()
        display.remove()
        player.sendActionBar(Component.empty())
    }

    fun finish(): Result? {
        if (!isValid) {
            return null
        }

        task.cancel()
        display.remove()
        player.sendActionBar(Component.empty())

        val pipeDisplay = ConnectingService.connect(from, to, pipe)
        return Result(pipeDisplay.getTo()!!, pipesUsed(from.position, to.position))
    }

    fun pathIntersectsBlocks()
        = to is ConnectingPointNewBlock && !to.position.block.type.isAir()
            || blocksOnPath(from.position, to.position).any { !it.type.isAir() }

    /**
     * Figures out where the target of the pipe is and adjusts the targetOffset and targetConnection
     */
    private fun recalculateTo() {
        val interaction = getTargetEntity(player)
        if (interaction != null ) {
            val fluidConnectionInteraction = EntityStorage.get(interaction)
            if (fluidConnectionInteraction is FluidPointInteraction) {
                val interactionOffset = interaction.location.add(Vector(0.0, interaction.height / 2, 0.0))
                        .subtract(from.position.location.toCenterLocation().toVector())
                        .toVector().toVector3f()
                val newTargetDifference = from.position
                    .minus(fluidConnectionInteraction.point.position)
                    .location.toVector().toVector3f()
                if (isValidTarget(interactionOffset, from.allowedFace) && isCardinalDirection(newTargetDifference)) {
                    to = ConnectingPointInteraction(fluidConnectionInteraction)
                    return
                }
            }
        }

        var distance = Float.MAX_VALUE
        distance = processAxis(Vector3i(1, 0, 0), distance)
        distance = processAxis(Vector3i(0, 1, 0), distance)
        processAxis(Vector3i(0, 0, 1), distance)
    }

    // helper function for recalculateTarget, returns new distance
    private fun processAxis(axis: Vector3i, distance: Float): Float {
        val playerLookPosition = player.eyeLocation.toVector().toVector3f()
        val playerLookDirection = player.eyeLocation.getDirection().toVector3f()
        val originPosition = from.position.location.toCenterLocation().toVector().toVector3f()

        val newTarget = getTargetOnAxis(playerLookPosition, playerLookDirection, originPosition, axis)
        val newAbsoluteTarget = Vector3f(newTarget).add(originPosition)
        val newDistance = findClosestDistanceBetweenLineAndPoint(newAbsoluteTarget, playerLookPosition, playerLookDirection)

        if (newDistance < distance && isValidTarget(newTarget, from.allowedFace)) {
            val newTargetPosition = from.position.plus(Vector3i(newTarget, RoundingMode.HALF_DOWN))
            val newTargetBlock = BlockStorage.get(newTargetPosition)
            to = if (newTargetBlock is FluidPipeMarker) {
                ConnectingPointPipeMarker(newTargetBlock)
            } else if (newTargetBlock is FluidPipeConnector) {
                ConnectingPointPipeConnector(newTargetBlock)
            } else {
                ConnectingPointNewBlock(newTargetPosition)
            }
            return newDistance
        }
        return distance
    }

    private val isPipeTypeValid: Boolean
        get() {
            val clonedTo = to // kotlin complains if no clone
            return when (clonedTo) {
                is ConnectingPointPipeMarker -> clonedTo.marker.getPipeDisplay()!!.pipe == pipe
                is ConnectingPointPipeConnector -> clonedTo.connector.pipe == pipe
                else -> true
            }
        }

    private val isPlacementValid: Boolean
        get() {
            // subdue kotlin
            val clonedTo = to
            val clonedFrom = from
            val toInteraction = clonedTo.interaction
            val fromInteraction = clonedFrom.interaction

            val startAndEndNotAlreadyConnected
                = !(toInteraction != null && from.connectedInteractions.contains(toInteraction.uuid))
                    && !(fromInteraction != null && to.connectedInteractions.contains(fromInteraction.uuid))

            val startAndEndEmptyIfNewBlock
                = !(clonedFrom is ConnectingPointNewBlock && !clonedFrom.position.block.type.isAir()
                    || to is ConnectingPointNewBlock && !clonedTo.position.block.type.isAir())

            val startAndEndNotSameIfNewBlock
                = !(from is ConnectingPointNewBlock
                    && to is ConnectingPointNewBlock
                    && from.position == to.position)

            return startAndEndNotAlreadyConnected
                    && startAndEndEmptyIfNewBlock
                    && startAndEndNotSameIfNewBlock
                    && !pathIntersectsBlocks()
        }

    companion object {
        fun pipesUsed(from: BlockPosition, to: BlockPosition)
            = blocksOnPath(from, to).size + 1

        /**
         * Does not include first or last block
         */
        fun blocksOnPath(from: BlockPosition, to: BlockPosition): MutableList<Block> {
            val originBlock = from.block
            val offset = to.location.toVector().toVector3i()
                .sub(originBlock.location.toVector().toVector3i())

            val blocks = mutableListOf<Block>()
            var block = originBlock
            // math.round to make it an integer - the length will already be an integer
            for (i in 0..<offset.length().roundToInt() - 1) {
                block = block.getRelative(vectorToBlockFace(offset))
                blocks.add(block)
            }

            return blocks
        }

        fun getTargetEntity(player: Player): Entity? {
            val playerLookPosition = player.eyeLocation.toVector().toVector3f()
            val playerLookDirection = player.eyeLocation.getDirection().toVector3f()

            val range = player.getAttribute(Attribute.ENTITY_INTERACTION_RANGE)!!.value
            val entities = player.getNearbyEntities(range, range, range)

            for (entity in entities) {
                val result = entity.boundingBox.rayTrace(
                    Vector.fromJOML(playerLookPosition),
                    Vector.fromJOML(playerLookDirection),
                    range
                )
                if (result != null) {
                    return entity
                }
            }

            return null
        }

        private fun isValidTarget(target: Vector3f, allowedFaceFrom: BlockFace?)
            = !target.equals(Vector3f(0f, 0f, 0f), 1.0e-3f)
                    && (allowedFaceFrom == null
                    || Vector3f(target).normalize().equals(allowedFaceFrom.getDirection().toVector3f(), 1.0e-3f))

        // Casts a ray where the player's looking and finds the closest block on a given axis
        private fun getTargetOnAxis(
            playerLookPosition: Vector3f?,
            playerLookDirection: Vector3f,
            origin: Vector3f,
            axis: Vector3i
        ): Vector3f {
            val solution =
                findClosestPointBetweenSkewLines(playerLookPosition, playerLookDirection, origin, Vector3f(axis))
            val lambda = Math.clamp(
                solution.roundToLong(),
                -PylonConfig.pipePlacementMaxDistance,
                PylonConfig.pipePlacementMaxDistance
            )
            return Vector3f(axis).mul(lambda.toFloat())
        }

        // returns lambda
        // r1 = p1 + lambda*d1 (line 1)
        // r2 = p2 + mu*d2 (line 2)
        // r3 = p3 + phi*d3 (an imagined perpendicular line between them used to solve for closest points)
        private fun findClosestPointBetweenSkewLines(p1: Vector3f?, d1: Vector3f, p2: Vector3f, d2: Vector3f): Float {
            val d3 = Vector3f(d1).cross(d2)
            // solve for lamdba, mu, phi using the matrix inversion method
            val mat = Matrix3f(d1, Vector3f(d2).mul(-1f), d3)
                .invert()
            val solution = Vector3f(p2).sub(p1).mul(mat)
            return solution.y
        }

        // https://math.stackexchange.com/questions/1905533/find-perpendicular-distance-from-point-to-line-in-3d
        private fun findClosestDistanceBetweenLineAndPoint(p: Vector3f, p1: Vector3f, d1: Vector3f): Float {
            val v = Vector3f(p).sub(p1)
            val t = Vector3f(v).dot(d1)
            val closestPoint = Vector3f(p1).add(Vector3f(d1).mul(t))
            return (Vector3f(closestPoint).sub(p)).length()
        }
    }
}