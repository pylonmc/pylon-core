package io.github.pylonmc.pylon.core.block

import io.github.pylonmc.pylon.core.persistence.PylonDataReader
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles

open class PylonBlockSchema(
    private val key: NamespacedKey,
    val material: Material,
    blockClass: Class<PylonBlock<PylonBlockSchema>>,
) : Keyed {

    internal val createConstructor: MethodHandle = try {
        MethodHandles.lookup().unreflectConstructor(blockClass.getConstructor(PylonBlockSchema::class.java))
    } catch (_: NoSuchMethodException) {
        throw NoSuchMethodException("Block '$key' is missing a create constructor")
    }

    internal val loadConstructor: MethodHandle = try {
        MethodHandles.lookup().unreflectConstructor(
            blockClass.getConstructor(
                PylonDataReader::class.java,
                Block::class.java
            )
        )
    } catch (_: NoSuchMethodException) {
        throw NoSuchMethodException("Block '$key' is missing a load constructor")
    }

    fun register() {
        PylonRegistry.BLOCKS.register(this)
    }

    override fun getKey(): NamespacedKey = key
}