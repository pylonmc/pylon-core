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

/**
 * Stores information about a Pylon block type, including its key, material, and class.
 *
 * You should not need to use this if you are not working on Pylon Core.
 */
class PylonBlockSchema(
    private val key: NamespacedKey,
    val material: Material,
    val blockClass: Class<out PylonBlock>,
) : Keyed {

    init {
        check(material.isBlock) { "Material $material is not a block" }
    }

    val addon = getAddon(key)

    val nameTranslationKey: TranslatableComponent
    val loreTranslationKey: TranslatableComponent
    val defaultWailaTranslationKey: TranslatableComponent

    init {
        val prefix = "pylon.${key.namespace}.item.${key.key}"
        nameTranslationKey = Component.translatable("$prefix.name")
        loreTranslationKey = Component.translatable("$prefix.lore")
        val default = "$prefix.waila"
        defaultWailaTranslationKey = if (addon.translator.languages.any { addon.translator.canTranslate(default, it) }) {
            Component.translatable(default)
        } else {
            nameTranslationKey
        }
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

    @JvmSynthetic
    internal fun create(block: Block, context: BlockCreateContext): PylonBlock {
        schemaCache[block.position] = this
        return createConstructor.invoke(block, context) as PylonBlock
    }

    @JvmSynthetic
    internal fun load(block: Block, pdc: PersistentDataContainer): PylonBlock {
        schemaCache[block.position] = this
        return loadConstructor.invoke(block, pdc) as PylonBlock
    }

    override fun getKey(): NamespacedKey = key

    override fun equals(other: Any?): Boolean = key == (other as? PylonBlockSchema)?.key

    override fun hashCode(): Int = key.hashCode()

    companion object {

        // This exists to avoid having to pass a key to the PylonBlock constructor
        internal val schemaCache: MutableMap<BlockPosition, PylonBlockSchema> = mutableMapOf()
    }
}
