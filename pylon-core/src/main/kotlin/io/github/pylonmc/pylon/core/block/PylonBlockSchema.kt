package io.github.pylonmc.pylon.core.block

import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.findConstructorMatching
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.persistence.PersistentDataContainer
import java.lang.invoke.MethodHandle

open class PylonBlockSchema(
    private val key: NamespacedKey,
    val material: Material,
    blockClass: Class<out PylonBlock<PylonBlockSchema>>,
) : Keyed {

    init {
        check(material.isBlock)
    }

    internal val createConstructor: MethodHandle = blockClass.findConstructorMatching(
        PylonBlockSchema::class.java,
        Block::class.java
    )
        ?: throw NoSuchMethodException("Block '$key' ($blockClass) is missing a create constructor (PylonBlockSchema, Block)")

    internal val loadConstructor: MethodHandle = blockClass.findConstructorMatching(
        javaClass,
        PersistentDataContainer::class.java,
        Block::class.java
    )
        ?: throw NoSuchMethodException("Block '$key' ($blockClass) is missing a load constructor (PylonBlockSchema, PersistentDataContainer, Block)")

    fun register() {
        PylonRegistry.BLOCKS.register(this)
    }

    override fun getKey(): NamespacedKey = key
}