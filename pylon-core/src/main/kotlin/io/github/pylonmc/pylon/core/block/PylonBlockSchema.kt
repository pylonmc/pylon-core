package io.github.pylonmc.pylon.core.block

import io.github.pylonmc.pylon.core.registry.Registries
import io.github.pylonmc.pylon.core.registry.RegistryKeys
import io.github.pylonmc.pylon.core.state.StateReader
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles

open class PylonBlockSchema(
    val key: NamespacedKey,
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
                StateReader::class.java,
                Block::class.java
            )
        )
    } catch (_: NoSuchMethodException) {
        throw NoSuchMethodException("'$key' is missing a load constructor")
    }

    // WHY IS THIS NEEDED
    override fun getKey(): NamespacedKey = key

    fun register() {
        Registries.getRegistry(RegistryKeys.BLOCKS).register(this)
    }
}