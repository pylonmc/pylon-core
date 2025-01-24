package io.github.pylonmc.pylon.core.persistence

import io.github.pylonmc.pylon.core.pluginInstance
import org.bukkit.NamespacedKey
import org.bukkit.persistence.ListPersistentDataType
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import java.nio.ByteBuffer

/**
 * Pylon implementation of PersistentDataContainers (https://docs.papermc.io/paper/dev/pdc)
 *
 * This implementation is tailored toward serializing everything into bytes for external storage on disk.
 *
 * Any nested PDCs are assumed to be PylonPersistentDataContainers.
 */
class PylonPersistentDataContainer(bytes: ByteArray) : PersistentDataContainer, PylonDataReader, PylonDataWriter {
    private val data: MutableMap<NamespacedKey, ByteArray> = HashMap()

    init {
        readFromBytes(bytes)
    }

    constructor() : this(byteArrayOf())

    /**
     * Constructor that also sets the ID of this PDC's holder. This should be used for creating PDCs for
     * holders such as blocks, but not for nested PDCs (they don't need ids)
     */
    constructor(id: NamespacedKey, bytes: ByteArray) : this(bytes) {
        set(idKey, PylonSerializers.NAMESPACED_KEY, id)
    }

    override val id
        get() = get(idKey, PylonSerializers.NAMESPACED_KEY)!!

    class PylonPersistentDataAdapterContext : PersistentDataAdapterContext {
        override fun newPersistentDataContainer(): PersistentDataContainer
                = PylonPersistentDataContainer(byteArrayOf())
    }

    override fun <P : Any, C : Any> has(key: NamespacedKey, type: PersistentDataType<P, C>): Boolean
        = get(key, type) != null

    override fun has(key: NamespacedKey): Boolean
        = data.containsKey(key)

    override fun <P : Any, C : Any> get(key: NamespacedKey, type: PersistentDataType<P, C>): C? {
        val x = data[key] ?: return null
        return type.fromPrimitive(bytesToPrimitive(type, x), adapterContext)
    }

    override fun <P : Any, C : Any> getOrDefault(
        key: NamespacedKey,
        type: PersistentDataType<P, C>,
        defaultValue: C
    ): C = get(key, type) ?: defaultValue

    override fun getKeys(): Set<NamespacedKey>
        = data.keys

    override fun isEmpty(): Boolean
        = data.isEmpty()

    override fun copyTo(other: PersistentDataContainer, replace: Boolean) {
        for ((key, entry) in data) {
            if (!replace and other.has(key)) {
                continue
            }
            other.set(key, PersistentDataType.BYTE_ARRAY, entry)
        }
    }

    override fun getAdapterContext(): PersistentDataAdapterContext
        = PylonPersistentDataAdapterContext()

    override fun serializeToBytes(): ByteArray {
        val keysAsBytes = data.keys.map {
            key -> PylonSerializers.NAMESPACED_KEY.toPrimitive(key, adapterContext).toByteArray(Charsets.UTF_8)
        }

        val bufferSize = keysAsBytes.zip(data.values).fold(0) {
            acc, (key, value) -> acc + 2 * Int.SIZE_BYTES + key.size + value.size
        }

        val buffer = ByteBuffer.allocate(bufferSize)
        for ((key, value) in keysAsBytes.zip(data.values)) {
            buffer.putInt(key.size)
            buffer.put(key)

            buffer.putInt(value.size)
            buffer.put(value)
        }

        return buffer.array()
    }

    override fun <P : Any?, C : Any?> set(key: NamespacedKey, type: PersistentDataType<P, C>, value: C & Any) {
        data[key] = primitiveToBytes(type, type.toPrimitive(value, adapterContext))
    }

    override fun remove(key: NamespacedKey) {
        data.remove(key)
    }

    override fun readFromBytes(bytes: ByteArray, clear: Boolean) {
        val buffer = ByteBuffer.wrap(bytes)
        while (buffer.hasRemaining()) {
            val keyLength = buffer.getInt()
            val keyBytes = ByteArray(keyLength)
            buffer.get(keyBytes)
            val key = PylonSerializers.NAMESPACED_KEY.fromPrimitive(keyBytes.toString(Charsets.UTF_8), adapterContext)

            val valueLength = buffer.getInt()
            val value = ByteArray(valueLength)
            buffer.get(value)

            data[key] = value
        }
    }

    companion object {
        @JvmField
        val idKey = NamespacedKey(pluginInstance, "pylon_id")
        @JvmField
        val context = PylonPersistentDataAdapterContext()

        private fun <P: Any?, C: Any?> primitiveToBytes(type: PersistentDataType<P, C>, primitive: Any): ByteArray {
            // Checks for the list type rather than for type.primitiveType for parity with PDC
            // (for some reason PDC doesn't count List<P> as a primitive unless accompanied by a ListPersistentDataType)
            if (type is ListPersistentDataType<*, *>) {
                val primitiveAsList = type.primitiveType.cast(primitive)
                val primitivesAsBytes = primitiveAsList.map {
                    val x = type.elementType()
                    primitiveToBytes(x, x.primitiveType.cast(it))
                }
                val bufferSize = primitivesAsBytes.fold(0) { acc, x -> acc + Int.SIZE_BYTES + x.size }
                val buffer = ByteBuffer.allocate(bufferSize)
                for (value in primitivesAsBytes) {
                    buffer.putInt(value.size)
                    buffer.put(value)
                }
                return buffer.array()
            }

            return when (type.primitiveType) {
                Byte::class.javaObjectType -> byteArrayOf(primitive as Byte)
                Short::class.javaObjectType -> ByteBuffer.allocate(Short.SIZE_BYTES).putShort(primitive as Short).array()
                Integer::class.javaObjectType -> ByteBuffer.allocate(Int.SIZE_BYTES).putInt(primitive as Int).array()
                Long::class.javaObjectType -> ByteBuffer.allocate(Long.SIZE_BYTES).putLong(primitive as Long).array()
                Float::class.javaObjectType -> ByteBuffer.allocate(Float.SIZE_BYTES).putFloat(primitive as Float).array()
                Double::class.javaObjectType -> ByteBuffer.allocate(Double.SIZE_BYTES).putDouble(primitive as Double).array()
                String::class.java -> (primitive as String).toByteArray(Charsets.UTF_8)
                ByteArray::class.java -> primitive as ByteArray
                IntArray::class.java -> {
                    val intArray = (primitive as IntArray)
                    val bytes = ByteBuffer.allocate(intArray.size * Int.SIZE_BYTES)
                    for (x in intArray) {
                        bytes.putInt(x)
                    }
                    bytes.array()
                }
                LongArray::class.java -> {
                    val intArray = (primitive as LongArray)
                    val bytes = ByteBuffer.allocate(intArray.size * Long.SIZE_BYTES)
                    for (x in intArray) {
                        bytes.putLong(x)
                    }
                    bytes.array()
                }
                // This is PylonPersistentDataContainer here because when we get to deserializing PDCs, there is no way to know
                // which implementor of PDC to serialize to. Therefore, we will only allow Pylon PDCs to be nested.
                PersistentDataContainer::class.java -> (primitive as PylonPersistentDataContainer).serializeToBytes()
                else -> error("Invalid primitive type '${type.primitiveType}'")
            }
        }

        // Sadly no way to 'switch' on the type T to avoid unchecked casting
        // See https://stackoverflow.com/questions/73523156/how-to-overload-function-with-different-return-types-and-the-same-parameters-in
        private fun <P: Any?, C: Any?> bytesToPrimitive(type: PersistentDataType<P, C>, bytes: ByteArray): P {
            // Checks for the list type rather than for type.primitiveType for parity with PDC
            // (for some reason PDC doesn't count List<P> as a primitive unless accompanied by a ListPersistentDataType)
            if (type is ListPersistentDataType<*, *>) {
                val buffer = ByteBuffer.wrap(bytes)
                val list: MutableList<Any> = mutableListOf()
                while (buffer.hasRemaining()) {
                    val length = buffer.getInt()
                    val value = ByteArray(length)
                    buffer.get(value)
                    val primitive = bytesToPrimitive(type.elementType(), value)
                    list.add(primitive)
                }
                @Suppress("UNCHECKED_CAST") // Needed to satisfy compiler but this should be safe
                return type.primitiveType.cast(list) as P
            }

            return when (type.primitiveType) {
                Byte::class.javaObjectType -> type.primitiveType.cast(bytes[0])
                Short::class.javaObjectType -> type.primitiveType.cast(ByteBuffer.wrap(bytes).getShort())
                Integer::class.javaObjectType -> type.primitiveType.cast(ByteBuffer.wrap(bytes).getInt())
                Long::class.javaObjectType -> type.primitiveType.cast(ByteBuffer.wrap(bytes).getLong())
                Float::class.javaObjectType -> type.primitiveType.cast(ByteBuffer.wrap(bytes).getFloat())
                Double::class.javaObjectType -> type.primitiveType.cast(ByteBuffer.wrap(bytes).getDouble())
                String::class.java -> type.primitiveType.cast(bytes.toString(Charsets.UTF_8))
                ByteArray::class.java -> type.primitiveType.cast(bytes)
                IntArray::class.java -> {
                    val buffer = ByteBuffer.wrap(bytes)
                    var array = intArrayOf()
                    while (buffer.hasRemaining()) {
                        array += buffer.getInt()
                    }
                    type.primitiveType.cast(array)
                }
                LongArray::class.java -> {
                    val buffer = ByteBuffer.wrap(bytes)
                    var array = longArrayOf()
                    while (buffer.hasRemaining()) {
                        array += buffer.getLong()
                    }
                    type.primitiveType.cast(array)
                }
                PersistentDataContainer::class.java -> type.primitiveType.cast(PylonPersistentDataContainer(bytes))
                else -> error("Invalid primitive type '${type.primitiveType}'")
            }
        }
    }
}
