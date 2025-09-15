@file:JvmName("PylonUtils")

package io.github.pylonmc.pylon.core.util

import com.destroystokyo.paper.profile.PlayerProfile
import com.mojang.brigadier.context.CommandContext
import io.github.pylonmc.pylon.core.addon.PylonAddon
import io.github.pylonmc.pylon.core.entity.display.transform.TransformUtil.yawToCardinalDirection
import io.github.pylonmc.pylon.core.fluid.PylonFluid
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.recipe.PylonRecipe
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
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.TranslationArgumentLike
import org.bukkit.NamespacedKey
import org.bukkit.block.BlockFace
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.util.Vector
import org.joml.RoundingMode
import org.joml.Vector3f
import org.joml.Vector3i
import kotlin.math.absoluteValue
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

fun isCardinalDirection(vector: Vector3i) = (vector.x != 0 && vector.y == 0 && vector.z == 0)
        || (vector.x == 0 && vector.y != 0 && vector.z == 0)
        || (vector.x == 0 && vector.y == 0 && vector.z != 0)

fun isCardinalDirection(vector: Vector3f)
    = (vector.x.absoluteValue > 1.0e-6 && vector.y.absoluteValue < 1.0e-6 && vector.z.absoluteValue < 1.0e-6)
        || (vector.x.absoluteValue < 1.0e-6 && vector.y.absoluteValue > 1.0e-6 && vector.z.absoluteValue < 1.0e-6)
        || (vector.x.absoluteValue < 1.0e-6 && vector.y.absoluteValue < 1.0e-6 && vector.z.absoluteValue > 1.0e-6)

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
fun rotateVectorToFace(vector: Vector3i, face: BlockFace) = when (face) {
    BlockFace.NORTH -> vector
    BlockFace.EAST -> Vector3i(-vector.z, vector.y, vector.x)
    BlockFace.SOUTH -> Vector3i(-vector.x, vector.y, -vector.z)
    BlockFace.WEST -> Vector3i(vector.z, vector.y, -vector.x)
    else -> throw IllegalArgumentException("$face is not a horizontal cardinal direction")
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

fun findRecipeFor(item: PylonItem): PylonRecipe? {
    // 1. if there's a recipe with the same key as the item, use that
    PylonRegistry.RECIPE_TYPES
        .map { it.getRecipe(item.schema.key) }
        .find { it != null }?.let { return it }

    // 2. if there's a recipe which produces *only* that item, use that
    // 3. if there's multiple recipes which produce only that item, choose the *lowest* one lexographically
    val singleOutputRecipes = PylonRegistry.RECIPE_TYPES.asSequence()
        .flatMap { it.recipes }
        .filter { recipe -> recipe.isOutput(item.stack) && recipe.results.size == 1 }
        .sortedBy { it.key }
        .toList()

    if (singleOutputRecipes.isNotEmpty()) return singleOutputRecipes.first()

    // 4. if there's a recipe which produces the item *alongside* other things, use that
    // 5. if there's multiple recipes which produce the item alongside other things, choose the *lowest* one lexographically
    val multiOutputRecipes = PylonRegistry.RECIPE_TYPES.asSequence()
        .flatMap { it.recipes }
        .filter { recipe -> recipe.isOutput(item.stack) }
        .sortedBy { it.key }
        .toList()

    if (multiOutputRecipes.isNotEmpty()) return multiOutputRecipes.first()

    return null
}

fun findRecipeFor(fluid: PylonFluid): PylonRecipe? {
    // 1. if there's a recipe with the same key as the item, use that
    PylonRegistry.RECIPE_TYPES
        .map { it.getRecipe(fluid.key) }
        .find { it != null }?.let { return it }

    // 2. if there's a recipe which produces *only* that item, use that
    // 3. if there's multiple recipes which produce only that item, choose the *lowest* one lexographically
    val singleOutputRecipes = PylonRegistry.RECIPE_TYPES.asSequence()
        .flatMap { it.recipes }
        .filter { recipe -> recipe.isOutput(fluid) && recipe.results.size == 1 }
        .sortedBy { it.key }
        .toList()

    if (singleOutputRecipes.isNotEmpty()) return singleOutputRecipes.first()

    // 4. if there's a recipe which produces the item *alongside* other things, use that
    // 5. if there's multiple recipes which produce the item alongside other things, choose the *lowest* one lexographically
    val multiOutputRecipes = PylonRegistry.RECIPE_TYPES.asSequence()
        .flatMap { it.recipes }
        .filter { recipe -> recipe.isOutput(fluid) }
        .sortedWith { a: PylonRecipe, b: PylonRecipe -> a.key.compareTo(b.key) }
        .toList()

    if (multiOutputRecipes.isNotEmpty()) return multiOutputRecipes.first()

    return null
}

fun isFakeEvent(event: Event): Boolean {
    return event.javaClass.name.contains("Fake")
}
