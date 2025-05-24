package io.github.pylonmc.pylon.core.block

import io.github.pylonmc.pylon.core.block.context.BlockCreateContext
import io.github.pylonmc.pylon.core.util.findConstructorMatching
import io.github.pylonmc.pylon.core.util.key.getAddon
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.persistence.PersistentDataContainer
import java.lang.invoke.MethodHandle

class PylonBlockSchema(
    private val key: NamespacedKey,
    val material: Material,
    blockClass: Class<out PylonBlock>,
) : Keyed {

    init {
        check(material.isBlock) { "Material $material is not a block" }
    }

    val addon = getAddon(key)

    @JvmSynthetic
    internal val createConstructor: MethodHandle = blockClass.findConstructorMatching(
        javaClass,
        Block::class.java,
        BlockCreateContext::class.java
    ) ?: throw NoSuchMethodException(
        "Block '$key' ($blockClass) is missing a create constructor (${javaClass.simpleName}, Block, BlockCreateContext)"
    )

    @JvmSynthetic
    internal val loadConstructor: MethodHandle = blockClass.findConstructorMatching(
        javaClass,
        Block::class.java,
        PersistentDataContainer::class.java
    ) ?: throw NoSuchMethodException(
        "Block '$key' ($blockClass) is missing a load constructor (${javaClass.simpleName}, Block, PersistentDataContainer)"
    )

    override fun getKey(): NamespacedKey = key

    override fun equals(other: Any?): Boolean = key == (other as? PylonBlockSchema)?.key

    override fun hashCode(): Int = key.hashCode()
}
