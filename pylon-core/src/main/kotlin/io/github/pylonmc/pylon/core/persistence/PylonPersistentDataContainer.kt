package io.github.pylonmc.pylon.core.persistence

import io.github.pylonmc.pylon.core.pluginInstance
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import java.nio.ByteBuffer

/*
 * Pylon implementation of PersistentDataContainers (https://docs.papermc.io/paper/dev/pdc)
 *
 * This implementation is tailored toward serializing everything into bytes for external storage on disk.
 *
 * Any nested PDCs are assumed to be PylonPersistentDataContainers.
 */
internal class PylonPersistentDataContainer(bytes: ByteArray) : PersistentDataContainer, PylonDataReader, PylonDataWriter {
    private val data: MutableMap<NamespacedKey, ByteArray> = HashMap()

    init {
        readFromBytes(bytes)
    }

    /*
     * Constructor that also sets the ID of this PDC's holder. This should be used for creating PDCs for
     * holders such as blocks, but not for nested PDCs (they don't need ids)
     */
    constructor(id: NamespacedKey, bytes: ByteArray) : this(bytes) {
        set(idKey, Serializers.NAMESPACED_KEY, id)
    }

    override val id
        get() = get(idKey, Serializers.NAMESPACED_KEY)!!

    private class PylonPersistentDataAdapterContext : PersistentDataAdapterContext {
        override fun newPersistentDataContainer(): PersistentDataContainer
                = PylonPersistentDataContainer(byteArrayOf())
    }

    override fun <P : Any, C : Any> has(key: NamespacedKey, type: PersistentDataType<P, C>): Boolean
        = get(key, type) != null

    override fun has(key: NamespacedKey): Boolean
        = data.containsKey(key)

    override fun <P : Any, C : Any> get(key: NamespacedKey, type: PersistentDataType<P, C>): C? {
        val x = data[key] ?: return null
        return type.fromPrimitive(bytesToPrimitive(type.primitiveType, x), adapterContext)
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
        val keysAsBytes = data.keys.map { key -> Serializers.NAMESPACED_KEY.toPrimitive(key, adapterContext) }
        val bufferSize = keysAsBytes.zip(data.values).fold(0) { acc, (key, value) -> acc + 2 * Int.SIZE_BYTES + key.size + value.size }

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
        data[key] = primitiveToBytes(type.primitiveType, type.toPrimitive(value, adapterContext))
    }

    override fun remove(key: NamespacedKey) {
        data.remove(key)
    }

    override fun readFromBytes(bytes: ByteArray, clear: Boolean) {
        val buffer = ByteBuffer.wrap(bytes)
        while (bytes.isEmpty()) {
            val keyLength = buffer.getInt()
            val keyBytes = ByteArray(keyLength)
            buffer.get(keyBytes)
            val key = Serializers.NAMESPACED_KEY.fromPrimitive(keyBytes, adapterContext)

            val valueLength = buffer.getInt()
            val value = ByteArray(valueLength)
            buffer.get(value)

            data[key] = value
        }
    }

    companion object {
        val idKey = NamespacedKey(pluginInstance, "pylon_id")

        private fun <T: Any?> primitiveToBytes(primitiveType: Class<T>, primitive: T): ByteArray
            = when (primitiveType) {
                Byte::class.java -> byteArrayOf(primitive as Byte)
                Short::class.java -> ByteBuffer.allocate(Short.SIZE_BYTES).putShort(primitive as Short).array()
                Integer::class.java -> ByteBuffer.allocate(Int.SIZE_BYTES).putInt(primitive as Int).array()
                Long::class.java -> ByteBuffer.allocate(Long.SIZE_BYTES).putLong(primitive as Long).array()
                Float::class.java -> ByteBuffer.allocate(Float.SIZE_BYTES).putFloat(primitive as Float).array()
                Double::class.java -> ByteBuffer.allocate(Double.SIZE_BYTES).putDouble(primitive as Double).array()
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
                else -> error("Unrecognized primitive type")
            }

        // Sadly no way to 'switch' on the type T to avoid unchecked casting
        // See https://stackoverflow.com/questions/73523156/how-to-overload-function-with-different-return-types-and-the-same-parameters-in
        @Suppress("UNCHECKED_CAST")
        private fun <T: Any?> bytesToPrimitive(primitiveType: Class<T>, bytes: ByteArray): T
            = when (primitiveType) {
                Byte::class.java -> bytes[0] as T
                Short::class.java -> ByteBuffer.wrap(bytes).getShort() as T
                Integer::class.java -> ByteBuffer.wrap(bytes).getInt() as T
                Long::class.java -> ByteBuffer.wrap(bytes).getLong() as T
                Float::class.java -> ByteBuffer.wrap(bytes).getFloat() as T
                Double::class.java -> ByteBuffer.wrap(bytes).getDouble() as T
                String::class.java -> bytes.toString(Charsets.UTF_8) as T
                ByteArray::class.java -> bytes as T
                IntArray::class.java -> {
                    val buffer = ByteBuffer.wrap(bytes)
                    var array = intArrayOf()
                    while (buffer.hasRemaining()) {
                        array += buffer.getInt()
                    }
                    array as T
                }
                LongArray::class.java -> {
                    val buffer = ByteBuffer.wrap(bytes)
                    var array = longArrayOf()
                    while (buffer.hasRemaining()) {
                        array += buffer.getLong()
                    }
                    array as T
                }
                PersistentDataContainer::class.java -> PylonPersistentDataContainer(bytes) as T
                else -> error("Unrecognized primitive type")
            }
    }
}
