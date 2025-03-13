package io.github.pylonmc.pylon.core.block

import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.findConstructorMatching
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.persistence.PersistentDataContainer
import java.lang.invoke.MethodHandle

open class PylonBlockSchema(
    private val key: NamespacedKey,
    blockClass: Class<out PylonBlock<*>>,
) : Keyed {

    @JvmSynthetic
    internal val createConstructor: MethodHandle = blockClass.findConstructorMatching(
        javaClass,
        Block::class.java,
        BlockCreateContext::class.java
    )
        ?: throw NoSuchMethodException("Block '$key' ($blockClass) is missing a create constructor (PylonBlockSchema, Block, BlockCreateContext)")

    @JvmSynthetic
    internal val loadConstructor: MethodHandle = blockClass.findConstructorMatching(
        javaClass,
        Block::class.java,
        PersistentDataContainer::class.java
    )
        ?: throw NoSuchMethodException("Block '$key' ($blockClass) is missing a load constructor (PylonBlockSchema, Block, PersistentDataContainer)")

    fun register() {
        PylonRegistry.BLOCKS.register(this)
    }

    override fun getKey(): NamespacedKey = key
}