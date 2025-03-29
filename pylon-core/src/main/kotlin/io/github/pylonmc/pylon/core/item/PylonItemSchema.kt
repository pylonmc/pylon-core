package io.github.pylonmc.pylon.core.item

import io.github.pylonmc.pylon.core.config.loadItemFromYaml
import io.github.pylonmc.pylon.core.item.PylonItem.Companion.idKey
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.item.builder.LoreBuilder
import io.github.pylonmc.pylon.core.persistence.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.pluginInstance
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.registry.RegistryHandler
import io.github.pylonmc.pylon.core.util.findConstructorMatching
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import java.lang.invoke.MethodHandle
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.reader

open class PylonItemSchema(
    private val key: NamespacedKey,
    @JvmSynthetic internal val itemClass: Class<out PylonItem<PylonItemSchema>>,
    itemSource: InitialItemSource
) : Keyed, RegistryHandler {

    constructor(
        key: NamespacedKey,
        itemClass: Class<out PylonItem<PylonItemSchema>>,
        template: ItemStack
    ) : this(key, itemClass, InitialItemSource.ItemStack(template))

    constructor(
        key: NamespacedKey,
        itemClass: Class<out PylonItem<PylonItemSchema>>,
        plugin: Plugin
    ) : this(key, itemClass, InitialItemSource.File(plugin))

    val template: ItemStack

    val itemStack: ItemStack
        get() = template.clone()

    @JvmSynthetic
    internal val loadConstructor: MethodHandle = itemClass.findConstructorMatching(
        javaClass,
        ItemStack::class.java
    )
        ?: throw NoSuchMethodException("Item '$key' ($itemClass) is missing a load constructor (PylonItemSchema, ItemStack)")

    init {
        val itemPath = getItemConfigPath(key)
        if (!itemPath.exists()) {
            itemSource.createItemFile(key)
        }
        template = itemPath.reader().use(::loadItemFromYaml)

        val addon = PylonRegistry.ADDONS.find { addon -> addon.key.namespace == key.namespace }
        checkNotNull(addon) { "Item does not have a corresponding addon; does your plugin call registerWithPylon()?" }
        ItemStackBuilder(template) // Modifies the template directly
            .editMeta { meta -> meta.persistentDataContainer.set(idKey, PylonSerializers.NAMESPACED_KEY, key) }
            .lore(LoreBuilder().addon(addon))
    }

    fun register() = apply {
        PylonRegistry.ITEMS.register(this)
    }

    override fun getKey(): NamespacedKey = key

    override fun equals(other: Any?): Boolean = key == (other as? PylonItemSchema)?.key

    override fun hashCode(): Int = key.hashCode()

    companion object {
        @JvmStatic
        val itemConfigDirectory: Path = pluginInstance.dataPath.resolve("items")

        init {
            itemConfigDirectory.createDirectories()
        }

        @JvmStatic
        fun getItemConfigPath(key: NamespacedKey): Path {
            val file = itemConfigDirectory.resolve("${key.namespace}/${key.key}.yml")
            file.parent.createDirectories()
            return file
        }
    }
}