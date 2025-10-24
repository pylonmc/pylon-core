@file:JvmName("PylonUtils")

package io.github.pylonmc.pylon.core.util

import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.addon.PylonAddon
import io.github.pylonmc.pylon.core.config.Config
import io.github.pylonmc.pylon.core.config.ConfigSection
import io.github.pylonmc.pylon.core.entity.display.transform.TransformUtil.yawToCardinalDirection
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.papermc.paper.datacomponent.DataComponentType
import io.papermc.paper.registry.keys.tags.BlockTypeTagKeys
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.TranslationArgumentLike
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataHolder
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector
import org.joml.RoundingMode
import org.joml.Vector3f
import org.joml.Vector3i
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import kotlin.math.absoluteValue
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Checks whether a [NamespacedKey] is from [addon]
 */
@JvmName("isKeyFromAddon")
fun NamespacedKey.isFromAddon(addon: PylonAddon): Boolean
    = namespace == addon.key.namespace

/**
 * Converts an orthogonal vector to a [BlockFace]
 *
 *  @return The face that the vector is facing
 *  @throws IllegalStateException if the vector is not pointing in a cardinal direction
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

/**
 * Converts an orthogonal vector to a [BlockFace]
 *
 *  @return The face that the vector is facing
 *  @throws IllegalStateException if the vector is not pointing in a cardinal direction
 */
fun vectorToBlockFace(vector: Vector3f) = vectorToBlockFace(Vector3i(vector, RoundingMode.HALF_DOWN))

/**
 * Converts an orthogonal vector to a [BlockFace]
 *
 *  @return The face that the vector is facing
 *  @throws IllegalStateException if the vector is not pointing in a cardinal direction
 */
// use toVector3f rather than toVector3i because toVector3i will floor components
fun vectorToBlockFace(vector: Vector) = vectorToBlockFace(vector.toVector3f())

/**
 * Rotates a BlockFace to the [player]'s reference frame.
 *
 * @param player The player to act as the reference frame. Where the player is facing becomes NORTH.
 * @param face The face to rotate
 * @param allowVertical Whether we should include UP and DOWN
 * @return The block face rotated to the player's reference frame
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

/**
 * Rotates [vector] to face a direction
 *
 * Assumes north to be the default direction (i.e. supplying north will result in no rotation)
 *
 * @param face Must be a horizontal cardinal direction (north, east, south, west)
 * @return The rotated vector
 */
fun rotateVectorToFace(vector: Vector3i, face: BlockFace) = when (face) {
    BlockFace.NORTH -> vector
    BlockFace.EAST -> Vector3i(-vector.z, vector.y, vector.x)
    BlockFace.SOUTH -> Vector3i(-vector.x, vector.y, -vector.z)
    BlockFace.WEST -> Vector3i(vector.z, vector.y, -vector.x)
    else -> throw IllegalArgumentException("$face is not a horizontal cardinal direction")
}

/**
 * @return Whether [vector] is a cardinal direction
 */
fun isCardinalDirection(vector: Vector3i) = (vector.x != 0 && vector.y == 0 && vector.z == 0)
        || (vector.x == 0 && vector.y != 0 && vector.z == 0)
        || (vector.x == 0 && vector.y == 0 && vector.z != 0)

/**
 * @return Whether [vector] is a cardinal direction
 */
fun isCardinalDirection(vector: Vector3f)
    = (vector.x.absoluteValue > 1.0e-6 && vector.y.absoluteValue < 1.0e-6 && vector.z.absoluteValue < 1.0e-6)
        || (vector.x.absoluteValue < 1.0e-6 && vector.y.absoluteValue > 1.0e-6 && vector.z.absoluteValue < 1.0e-6)
        || (vector.x.absoluteValue < 1.0e-6 && vector.y.absoluteValue < 1.0e-6 && vector.z.absoluteValue > 1.0e-6)

/**
 * @return The addon that [key] belongs to
 */
fun getAddon(key: NamespacedKey): PylonAddon =
    PylonRegistry.Companion.ADDONS.find { addon -> addon.key.namespace == key.namespace }
        ?: error("Key does not have a corresponding addon; does your addon call registerWithPylon()?")

/**
 * Attaches arguments to a component and all its children.
 *
 * @param args List of arguments to attach
 * @return The component with the arguments attached
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

/**
 * (Heuristically) checks whether an event is 'fake' (by checking if it has 'Fake' in its name)
 *
 * 'Fake' events are often used to check actions before performing them.
 */
fun isFakeEvent(event: Event): Boolean {
    return event.javaClass.name.contains("Fake")
}

/**
 * [BlockFace.UP], [BlockFace.DOWN], [BlockFace.EAST], [BlockFace.WEST], [BlockFace.SOUTH], [BlockFace.NORTH]
 */
@JvmField
val IMMEDIATE_FACES: Array<BlockFace> = arrayOf(
    BlockFace.UP,
    BlockFace.DOWN,
    BlockFace.EAST,
    BlockFace.WEST,
    BlockFace.SOUTH,
    BlockFace.NORTH
)

/**
 * Same as [IMMEDIATE_FACES] but includes diagonal faces, not including the vertical directions.
 */
@JvmField
val IMMEDIATE_FACES_WITH_DIAGONALS: Array<BlockFace> = arrayOf(
    BlockFace.UP,
    BlockFace.DOWN,
    BlockFace.EAST,
    BlockFace.WEST,
    BlockFace.SOUTH,
    BlockFace.NORTH,
    BlockFace.NORTH_EAST,
    BlockFace.NORTH_WEST,
    BlockFace.SOUTH_EAST,
    BlockFace.SOUTH_WEST,
    BlockFace.EAST
)

@JvmSynthetic
internal fun pylonKey(key: String): NamespacedKey = NamespacedKey(PylonCore, key)

@JvmSynthetic
internal fun Class<*>.findConstructorMatching(vararg types: Class<*>): MethodHandle? {
    return declaredConstructors.firstOrNull {
        it.parameterTypes.size == types.size &&
                it.parameterTypes.zip(types).all { (param, given) -> given.isSubclassOf(param) }
    }?.let(MethodHandles.lookup()::unreflectConstructor)
}

// I can never remember which way around `isAssignableFrom` goes,
// so this is a helper function to make it more readable
@JvmSynthetic
private fun Class<*>.isSubclassOf(other: Class<*>): Boolean = other.isAssignableFrom(this)

/**
 * Small helper function to convert a minimessage string (eg: '<red>bruh') into a component
 * @param string The string to turn into a component
 * @returns The string as a component
 */
@JvmSynthetic
fun fromMiniMessage(string: String): Component = MiniMessage.miniMessage().deserialize(string)

/**
 * Finds a Pylon item in an inventory. Use this to find Pylon items instead of traditional
 * find methods, because this will compare Pylon IDs.
 *
 * @param inventory The inventory to search
 * @param targetItem The item to find. Items will be compared by their Pylon ID
 * @return The slot containing the item, or null if no item was found
 */
fun findPylonItemInInventory(inventory: Inventory, targetItem: PylonItem): Int? {
    for (i in 0..<inventory.size) {
        val item = inventory.getItem(i)?.let {
            PylonItem.fromStack(it)
        }
        if (item == targetItem) {
            return i
        }
    }
    return null
}

/**
 * Compares two Pylon items to check if they have the same Pylon ID. If
 * neither item is a Pylon item, the material will be compared instead.
 *
 * @return Whether the items are the same.
 */
fun ItemStack?.isPylonSimilar(item2: ItemStack?): Boolean {
    // Both items null
    if (this == null && item2 == null) {
        return true
    }

    // One item null, one not null
    if (!(this != null && item2 != null)) {
        return false
    }

    val pylonItem1 = PylonItem.fromStack(this)
    val pylonItem2 = PylonItem.fromStack(item2)

    // Both pylon items null
    if (pylonItem1 == null && pylonItem2 == null) {
        return this.isSimilar(item2)
    }

    // One pylon item null, one not null
    if (!(pylonItem1 != null && pylonItem2 != null)) {
        return false
    }

    return pylonItem1.schema.key == pylonItem2.schema.key
}

@JvmSynthetic
inline fun <reified T> ItemStack?.isPylonAndIsNot(): Boolean {
    val pylonItem = PylonItem.fromStack(this)
    return pylonItem != null && pylonItem !is T
}

@JvmSynthetic
@Suppress("UnstableApiUsage")
inline fun <T : Any> ItemStack.editData(
    type: DataComponentType.Valued<T>,
    block: (T) -> T
): ItemStack {
    val data = getData(type) ?: return this
    setData(type, block(data))
    return this
}

@JvmSynthetic
@Suppress("UnstableApiUsage")
inline fun <T : Any> ItemStack.editDataOrDefault(
    type: DataComponentType.Valued<T>,
    block: (T) -> T
): ItemStack {
    val data = getData(type) ?: this.type.getDefaultData(type) ?: return this
    setData(type, block(data))
    return this
}

@JvmSynthetic
@Suppress("UnstableApiUsage")
inline fun <T : Any> ItemStack.editDataOrSet(
    type: DataComponentType.Valued<T>,
    block: (T?) -> T
): ItemStack {
    setData(type, block(getData(type)))
    return this
}

/**
 * Wrapper around [PersistentDataContainer.set] that allows nullable values to be passed
 *
 * @param value The value to set. If this is null, the key will be removed from the container
 */
fun <P, C> PersistentDataContainer.setNullable(key: NamespacedKey, type: PersistentDataType<P, C>, value: C?) {
    if (value != null) {
        set(key, type, value)
    } else {
        remove(key)
    }
}

/**
 * Acts as a property delegate for stuff contained inside a [PersistentDataContainer]
 * For example:
 * ```
 * val numberOfTimesJumped: Int by persistentData(NamespacedKey(yourPlugin, "jumped"), PersistentDataType.INTEGER) { 0 }
 * ```
 */
@JvmSynthetic
inline fun <T> persistentData(
    key: NamespacedKey,
    type: PersistentDataType<*, T & Any>,
    crossinline default: () -> T
) = object : ReadWriteProperty<PersistentDataHolder, T> {

    override fun getValue(thisRef: PersistentDataHolder, property: KProperty<*>): T {
        return thisRef.persistentDataContainer.get(key, type) ?: default()
    }

    override fun setValue(thisRef: PersistentDataHolder, property: KProperty<*>, value: T) {
        if (value == null) {
            thisRef.persistentDataContainer.remove(key)
        } else {
            thisRef.persistentDataContainer.set(key, type, value)
        }
    }
}

/**
 * Same as [persistentData] but with a default value that is constant
 */
@JvmSynthetic
fun <T> persistentData(
    key: NamespacedKey,
    type: PersistentDataType<*, T & Any>,
    default: T
) = persistentData(key, type) { default }

/**
 * Merges config from addons to the Pylon config directory.
 * Used for stuff like item settings and language files.
 *
 * Returns the configuration read and merged from the resource.
 * If the file does not exist in the resource but already exists
 * at the [to] path, reads and returns the file at the [to] path.
 *
 * @param from The path to the config file. Must be a YAML file.
 * @return The merged config
 */
internal fun mergeGlobalConfig(addon: PylonAddon, from: String, to: String, warnMissing: Boolean = true): Config {
    require(from.endsWith(".yml")) { "Config file must be a YAML file" }
    require(to.endsWith(".yml")) { "Config file must be a YAML file" }
    val cached = globalConfigCache[from to to]
    if (cached != null) {
        return cached
    }
    val globalConfig = PylonCore.dataFolder.resolve(to)
    if (!globalConfig.exists()) {
        globalConfig.parentFile.mkdirs()
        globalConfig.createNewFile()
    }
    val config = Config(globalConfig)
    val resource = addon.javaPlugin.getResource(from)
    if (resource == null) {
        if (warnMissing) PylonCore.logger.warning("Resource not found: $from")
    } else {
        val newConfig = resource.reader().use(YamlConfiguration::loadConfiguration)
        config.internalConfig.setDefaults(newConfig)
        config.internalConfig.options().copyDefaults(true)
        config.merge(ConfigSection(newConfig))
        config.save()
    }
    globalConfigCache[from to to] = config
    return config
}

private val globalConfigCache: MutableMap<Pair<String, String>, Config> = mutableMapOf()

val Block.replaceableOrAir: Boolean
    get() = type.isAir || isReplaceable

fun ItemStack.vanillaDisplayName(): Component
    = effectiveName().let {
        val wrapped = Component.translatable("chat.square_brackets", it)
        if (!this.isEmpty) {
            wrapped.hoverEvent(this.asHoverEvent())
        }
        return wrapped
    }

val Component.plainText: String
    get() = PlainTextComponentSerializer.plainText().serialize(this)

fun pickaxeMineable() = Registry.BLOCK.getTag(BlockTypeTagKeys.MINEABLE_PICKAXE)
fun axeMineable() = Registry.BLOCK.getTag(BlockTypeTagKeys.MINEABLE_AXE)
fun shovelMineable() = Registry.BLOCK.getTag(BlockTypeTagKeys.MINEABLE_SHOVEL)
fun hoeMineable() = Registry.BLOCK.getTag(BlockTypeTagKeys.MINEABLE_HOE)