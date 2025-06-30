@file:JvmName("PylonUtils")

package io.github.pylonmc.pylon.core.util

import io.github.pylonmc.pylon.core.addon.PylonAddon
import io.github.pylonmc.pylon.core.entity.display.transform.TransformUtil.yawToCardinalDirection
import org.bukkit.NamespacedKey
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import org.joml.RoundingMode
import org.joml.Vector3f
import org.joml.Vector3i

/*
This file is for public general utils that Java can make use of. See also `InternalUtils.kt`.
 */

fun NamespacedKey.isFromAddon(addon: PylonAddon): Boolean {
    return namespace == addon.key.namespace
}

fun vectorToBlockFace(vector: Vector3i): BlockFace {
    return if (vector.x > 0 && vector.y == 0 && vector.z == 0) {
        BlockFace.EAST
    } else if (vector.x < 0 && vector.y == 0 && vector.z == 0) {
        BlockFace.WEST
    } else if (vector.x == 0 && vector.y > 0 && vector.z == 0) {
        BlockFace.UP
    } else if (vector.x == 0 && vector.y < 0 && vector.z == 0) {
        BlockFace.DOWN
    } else if (vector.x == 0 && vector.y == 0 && vector.z > 0) {
        BlockFace.SOUTH
    } else if (vector.x == 0 && vector.y == 0 && vector.z < 0) {
        BlockFace.NORTH
    } else {
        throw IllegalStateException("Vector $vector cannot be turned into a block face")
    }
}

fun vectorToBlockFace(vector: Vector3f) = vectorToBlockFace(Vector3i(vector, RoundingMode.HALF_DOWN))

// use toVector3f rather than toVector3i because toVector3i will floor components
fun vectorToBlockFace(vector: Vector) = vectorToBlockFace(vector.toVector3f())

/**
 * Rotates a BlockFace to the player's reference frame. Where the player is facing becomes NORTH.
 */
fun rotateToPlayerFacing(player: Player, face: BlockFace, allowVertical: Boolean): BlockFace {
    var vector = face.direction.clone().rotateAroundY(yawToCardinalDirection(player.eyeLocation.yaw).toDouble())
    if (allowVertical) {
        val rightVector = vector.getCrossProduct(Vector(0.0, 1.0, 0.0)) // never thought cross product would come in useful but here we go
        vector = vector.rotateAroundNonUnitAxis(rightVector, -yawToCardinalDirection(player.eyeLocation.pitch).toDouble())
    }
    return vectorToBlockFace(vector)
}