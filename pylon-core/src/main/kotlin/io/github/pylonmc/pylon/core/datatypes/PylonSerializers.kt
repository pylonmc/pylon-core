package io.github.pylonmc.pylon.core.datatypes

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
    val SET = SetPersistentDataType.Companion

    @JvmField
    val MAP = MapPersistentDataType.Companion

    @JvmField
    val ENUM = EnumPersistentDataType.Companion

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
    val CHUNK_POSITION = ChunkPositionPersistentDataType

    @JvmField
    val LOCATION = LocationPersistentDataType

    @JvmField
    val ITEM_STACK = ItemStackPersistentDataType

    @JvmField
    val INVENTORY = InventoryPersistentDataType
}