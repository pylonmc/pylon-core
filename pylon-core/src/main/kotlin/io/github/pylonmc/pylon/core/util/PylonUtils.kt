@file:JvmName("PylonUtils")

package io.github.pylonmc.pylon.core.util

import com.destroystokyo.paper.profile.PlayerProfile
import com.mojang.brigadier.context.CommandContext
import io.github.pylonmc.pylon.core.addon.PylonAddon
import io.github.pylonmc.pylon.core.entity.display.transform.TransformUtil.yawToCardinalDirection
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver
import io.papermc.paper.command.brigadier.argument.resolvers.FinePositionResolver
import io.papermc.paper.command.brigadier.argument.resolvers.PlayerProfileListResolver
import io.papermc.paper.command.brigadier.argument.resolvers.RotationResolver
import io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import io.papermc.paper.math.BlockPosition
import io.papermc.paper.math.FinePosition
import io.papermc.paper.math.Rotation
import org.bukkit.Material
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.TranslationArgumentLike
import org.bukkit.NamespacedKey
import org.bukkit.block.BlockFace
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import org.joml.RoundingMode
import org.joml.Vector3f
import org.joml.Vector3i
import kotlin.reflect.typeOf

fun NamespacedKey.isFromAddon(addon: PylonAddon): Boolean {
    return namespace == addon.key.namespace
}

/**
 * Converts an approximate orthogonal vector to a [BlockFace]
 */
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
        // never thought cross product would come in useful but here we go
        val rightVector = vector.getCrossProduct(Vector(0.0, 1.0, 0.0))
        vector =
            vector.rotateAroundNonUnitAxis(rightVector, -yawToCardinalDirection(player.eyeLocation.pitch).toDouble())
    }
    return vectorToBlockFace(vector)
}

fun isCardinalDirection(vector: Vector3i)
    = (vector.x != 0 && vector.y == 0 && vector.z == 0)
        || (vector.x == 0 && vector.y != 0 && vector.z == 0)
        || (vector.x == 0 && vector.y == 0 && vector.z != 0)

fun getAddon(key: NamespacedKey): PylonAddon =
    PylonRegistry.Companion.ADDONS.find { addon -> addon.key.namespace == key.namespace }
        ?: error("Key does not have a corresponding addon; does your addon call registerWithPylon()?")

/**
 * Rotates a vector to face a direction
 *
 * The direction given must be a horizontal cardinal direction (north, east, south, west)
 *
 * Assumes north to be the default direction (supplying north will result in no rotation)
 */
fun rotateVectorToFace(vector: Vector3i, face: BlockFace)
    = when (face) {
        BlockFace.NORTH -> vector
        BlockFace.EAST -> Vector3i(-vector.z, vector.y, vector.x)
        BlockFace.SOUTH -> Vector3i(-vector.x, vector.y, -vector.z)
        BlockFace.WEST -> Vector3i(vector.z, vector.y, -vector.x)
        else -> throw IllegalArgumentException("$face is not a horizontal cardinal direction")
    }

fun itemFromName(name: String): ItemStack? {
    if (name.contains(':')) {
        val namespacedKey = NamespacedKey.fromString(name)
        if (namespacedKey != null) {
            val pylonItem = PylonRegistry.ITEMS[namespacedKey]
            if (pylonItem != null) {
                return pylonItem.itemStack
            }
        }
    }

    val material = Material.getMaterial(name.uppercase())
    if (material != null) {
        return ItemStack(material)
    }

    return null
}

fun wrapText(text: String, limit: Int): List<String> {
    val words = text.split(" ")
    val lines = mutableListOf<String>()
    var currentLine = StringBuilder()

    for (word in words) {
        if (currentLine.length + word.length + 1 > limit) {
            currentLine.append(' ')
            lines.add(currentLine.toString())
            currentLine = StringBuilder()
        }
        if (currentLine.isNotEmpty()) {
            currentLine.append(" ")
        }
        currentLine.append(word)
    }
    if (currentLine.isNotEmpty()) {
        lines.add(currentLine.toString())
    }
    return lines
}

@JvmSynthetic
@Suppress("UnstableApiUsage")
inline fun <reified T> CommandContext<CommandSourceStack>.getArgument(name: String): T {
    return when (typeOf<T>()) {
        typeOf<BlockPosition>() -> getArgument(name, BlockPositionResolver::class.java).resolve(source)
        typeOf<List<Entity>>() -> getArgument(name, EntitySelectorArgumentResolver::class.java).resolve(source)
        typeOf<Entity>() -> getArgument(name, EntitySelectorArgumentResolver::class.java).resolve(source).first()
        typeOf<FinePosition>() -> getArgument(name, FinePositionResolver::class.java).resolve(source)
        typeOf<List<PlayerProfile>>() -> getArgument(name, PlayerProfileListResolver::class.java).resolve(source)
        typeOf<PlayerProfile>() -> getArgument(name, PlayerProfileListResolver::class.java).resolve(source).first()
        typeOf<List<Player>>() -> getArgument(name, PlayerSelectorArgumentResolver::class.java).resolve(source)
        typeOf<Player>() -> getArgument(name, PlayerSelectorArgumentResolver::class.java).resolve(source).first()
        typeOf<Rotation>() -> getArgument(name, RotationResolver::class.java).resolve(source)
        else -> getArgument(name, T::class.java)
    } as T
}

/**
 * Attaches arguments to a component and all its children.
 */
@JvmName("attachArguments")
fun Component.withArguments(args: List<TranslationArgumentLike>): Component {
    if (args.isEmpty()) return this
    var result = this
    if (this is TranslatableComponent) {
        result = this.arguments(args)
    }
    return result.children(result.children().map { it.withArguments(args) })
}
