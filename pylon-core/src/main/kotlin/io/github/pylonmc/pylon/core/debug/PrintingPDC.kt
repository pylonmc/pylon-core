package io.github.pylonmc.pylon.core.debug

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

// O wise one, what is my purpose in life?
// P R I N T E R
class PrintingPDC(private val audience: Audience) : PersistentDataContainer {

    private val data = mutableMapOf<NamespacedKey, Any>()

    override fun <P : Any, C : Any> set(
        key: NamespacedKey,
        type: PersistentDataType<P, C>,
        value: C
    ) {
        data[key] = value
        audience.sendMessage(
            Component.translatable(
                "pylon.pyloncore.message.debug.pdc",
                Component.text(key.toString()),
                Component.text(value.toString())
            )
        )
    }

    override fun remove(key: NamespacedKey) {
        data.remove(key)
    }

    override fun readFromBytes(bytes: ByteArray, clear: Boolean) {
        throw UnsupportedOperationException()
    }

    override fun <P : Any, C : Any> has(
        key: NamespacedKey,
        type: PersistentDataType<P, C>
    ): Boolean {
        return key in data
    }

    override fun has(key: NamespacedKey): Boolean {
        return key in data
    }

    override fun <P : Any, C : Any> get(
        key: NamespacedKey,
        type: PersistentDataType<P, C>
    ): C? {
        @Suppress("UNCHECKED_CAST")
        return data[key] as? C
    }

    override fun <P : Any, C : Any> getOrDefault(
        key: NamespacedKey,
        type: PersistentDataType<P, C>,
        defaultValue: C
    ): C {
        return get(key, type) ?: defaultValue
    }

    override fun getKeys(): Set<NamespacedKey> {
        return data.keys
    }

    override fun isEmpty(): Boolean {
        return data.isEmpty()
    }

    override fun copyTo(other: PersistentDataContainer, replace: Boolean) {
        throw UnsupportedOperationException()
    }

    override fun getAdapterContext(): PersistentDataAdapterContext {
        throw UnsupportedOperationException()
    }

    override fun serializeToBytes(): ByteArray {
        throw UnsupportedOperationException()
    }
}