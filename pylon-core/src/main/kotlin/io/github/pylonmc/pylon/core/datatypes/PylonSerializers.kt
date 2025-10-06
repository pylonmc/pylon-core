package io.github.pylonmc.pylon.core.datatypes

import io.github.pylonmc.pylon.core.fluid.PylonFluid
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.waila.Waila
import org.bukkit.Material
import org.bukkit.Registry
import org.bukkit.block.BlockFace
import org.bukkit.persistence.PersistentDataType

/**
 * A collection of various [PersistentDataType]s for use by Pylon and Pylon addons
 */
@Suppress("unused")
object PylonSerializers {
    @JvmField
    val BYTE = PersistentDataType.BYTE!!

    @JvmField
    val SHORT = PersistentDataType.SHORT!!

    @JvmField
    val INTEGER = PersistentDataType.INTEGER!!

    @JvmField
    val LONG = PersistentDataType.LONG!!

    @JvmField
    val FLOAT = PersistentDataType.FLOAT!!

    @JvmField
    val DOUBLE = PersistentDataType.DOUBLE!!

    @JvmField
    val BOOLEAN = PersistentDataType.BOOLEAN!!

    @JvmField
    val STRING = PersistentDataType.STRING!!

    @JvmField
    val CHAR = CharPersistentDataType

    @JvmField
    val BYTE_ARRAY = PersistentDataType.BYTE_ARRAY!!

    @JvmField
    val INTEGER_ARRAY = PersistentDataType.INTEGER_ARRAY!!

    @JvmField
    val LONG_ARRAY = PersistentDataType.LONG_ARRAY!!

    @JvmField
    val TAG_CONTAINER = PersistentDataType.TAG_CONTAINER!!

    @JvmField
    val LIST = PersistentDataType.LIST!!

    @JvmField
    val SET = SetPersistentDataType

    @JvmField
    val MAP = MapPersistentDataType

    @JvmField
    val ENUM = EnumPersistentDataType

    @JvmField
    val NAMESPACED_KEY = NamespacedKeyPersistentDataType

    @JvmField
    val UUID = UUIDPersistentDataType

    @JvmField
    val VECTOR = VectorPersistentDataType

    @JvmField
    val WORLD = WorldPersistentDataType

    @JvmField
    val BLOCK_POSITION = BlockPositionPersistentDataType

    @JvmField
    val BLOCK_FACE = EnumPersistentDataType(BlockFace::class.java)

    @JvmField
    val CHUNK_POSITION = ChunkPositionPersistentDataType

    @JvmField
    val LOCATION = LocationPersistentDataType

    @JvmField
    val ITEM_STACK = ItemStackPersistentDataType

    @JvmField
    val INVENTORY = InventoryPersistentDataType

    @JvmField
    val KEYED = KeyedPersistentDataType

    @JvmField
    val MATERIAL = KEYED.keyedTypeFrom<Material>(Registry.MATERIAL::getOrThrow)

    @JvmField
    val PYLON_FLUID = KEYED.keyedTypeFrom<PylonFluid>(PylonRegistry.FLUIDS::getOrThrow)

    @JvmSynthetic
    internal val FLUID_BUFFER_DATA = FluidBufferDataPersistentDataType

    @JvmSynthetic
    internal val FLUID_TANK_DATA = FluidTankDataPersistentDataType

    @JvmSynthetic
    internal val SIMPLE_MULTIBLOCK_DATA = SimpleMultiblockDataPersistentDataType

    @JvmSynthetic
    internal val TICKING_BLOCK_DATA = TickingBlockPersistentDataType

    @JvmField
    val FLUID_CONNECTION_POINT = FluidConnectionPointDataType

    @JvmSynthetic
    internal val WAILA_TYPE = EnumPersistentDataType(Waila.Type::class.java)

    @JvmSynthetic
    internal val PLAYER_WAILA_CONFIG = PlayerWailaConfigPersistentDataType
}
