package io.github.pylonmc.pylon.core.block

import io.github.pylonmc.pylon.core.block.context.BlockCreateContext
import io.github.pylonmc.pylon.core.i18n.PylonTranslator.Companion.translator
import io.github.pylonmc.pylon.core.util.findConstructorMatching
import io.github.pylonmc.pylon.core.util.getAddon
import io.github.pylonmc.pylon.core.util.position.BlockPosition
import io.github.pylonmc.pylon.core.util.position.position
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
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

    val defaultBlockTranslationKey: TranslatableComponent

    init {
        val prefix = "pylon.${key.namespace}.item.${key.key}"
        val default = "$prefix.waila"
        defaultBlockTranslationKey = Component.translatable(
            if (addon.translator.languages.any { addon.translator.canTranslate(default, it) }) default else "$prefix.name"
        )
    }

    private val createConstructor: MethodHandle = blockClass.findConstructorMatching(
        Block::class.java,
        BlockCreateContext::class.java
    ) ?: throw NoSuchMethodException(
        "Block '$key' ($blockClass) is missing a create constructor (${javaClass.simpleName}, Block, BlockCreateContext)"
    )

    private val loadConstructor: MethodHandle = blockClass.findConstructorMatching(
        Block::class.java,
        PersistentDataContainer::class.java
    ) ?: throw NoSuchMethodException(
        "Block '$key' ($blockClass) is missing a load constructor (${javaClass.simpleName}, Block, PersistentDataContainer)"
    )

    fun create(block: Block, context: BlockCreateContext): PylonBlock {
        schemaCache[block.position] = this
        return createConstructor.invoke(block, context) as PylonBlock
    }

    fun load(block: Block, pdc: PersistentDataContainer): PylonBlock {
        schemaCache[block.position] = this
        return loadConstructor.invoke(block, pdc) as PylonBlock
    }

    override fun getKey(): NamespacedKey = key

    override fun equals(other: Any?): Boolean = key == (other as? PylonBlockSchema)?.key

    override fun hashCode(): Int = key.hashCode()

    companion object {

        // This exists to avoid having to pass a key to the PylonBlock constructor
        val schemaCache: MutableMap<BlockPosition, PylonBlockSchema> = mutableMapOf()
    }
}
