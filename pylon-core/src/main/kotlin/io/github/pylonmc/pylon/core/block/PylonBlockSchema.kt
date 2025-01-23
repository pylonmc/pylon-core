package io.github.pylonmc.pylon.core.block

import io.github.pylonmc.pylon.core.persistence.PylonDataReader
import io.github.pylonmc.pylon.core.registry.PylonRegistries
import io.github.pylonmc.pylon.core.registry.PylonRegistryKeys
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

    internal val placeConstructor: MethodHandle = try {
        MethodHandles.lookup().unreflectConstructor(blockClass.getConstructor(PylonBlockSchema::class.java))
    } catch (_: NoSuchMethodException) {
        throw NoSuchMethodException("'$key' is missing a place constructor")
    }

    internal val loadConstructor: MethodHandle = try {
        MethodHandles.lookup().unreflectConstructor(
            blockClass.getConstructor(
                PylonDataReader::class.java,
                Block::class.java
            )
        )
    } catch (_: NoSuchMethodException) {
        throw NoSuchMethodException("'$key' is missing a load constructor")
    }

    override fun getKey(): NamespacedKey = key

    fun register() {
        PylonRegistries.getRegistry(PylonRegistryKeys.BLOCKS).register(this)
    }
}