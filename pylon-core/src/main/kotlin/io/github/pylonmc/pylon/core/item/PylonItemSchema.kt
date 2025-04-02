package io.github.pylonmc.pylon.core.item

import io.github.pylonmc.pylon.core.config.Config
import io.github.pylonmc.pylon.core.config.ConfigSection
import io.github.pylonmc.pylon.core.item.PylonItem.Companion.idKey
import io.github.pylonmc.pylon.core.persistence.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.pluginInstance
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.registry.RegistryHandler
import io.github.pylonmc.pylon.core.util.findConstructorMatching
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import java.lang.invoke.MethodHandle
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.exists

open class PylonItemSchema(
    private val key: NamespacedKey,
    @JvmSynthetic internal val itemClass: Class<out PylonItem<PylonItemSchema>>,
    private val template: ItemStack
) : Keyed, RegistryHandler {

    val itemStack: ItemStack
        get() = template.clone()

    @JvmSynthetic
    internal val loadConstructor: MethodHandle = itemClass.findConstructorMatching(
        javaClass,
        ItemStack::class.java
    )
        ?: throw NoSuchMethodException("Item '$key' ($itemClass) is missing a load constructor (PylonItemSchema, ItemStack)")

    val settings: Config

    init {
        val addon = PylonRegistry.ADDONS.find { addon -> addon.key.namespace == key.namespace }
        if (addon == null) {
            throw IllegalStateException("Item does not have a corresponding addon; does your plugin call registerWithPylon()?")
        }
        ItemStackBuilder(template) // Modifies the template directly
            .editMeta { meta -> meta.persistentDataContainer.set(idKey, PylonSerializers.NAMESPACED_KEY, key) }
            .lore(LoreBuilder().addon(addon))

        val settingsFile = getItemFile(key)
        if (!settingsFile.exists()) {
            settingsFile.parent.createDirectories()
            settingsFile.createFile()
        }
        settings = Config(settingsFile.toFile())
        val resource = addon.javaPlugin.getResource("settings/item/${keyPath(key)}")
        if (resource != null) {
            val newConfig = resource.reader().use { ConfigSection(YamlConfiguration.loadConfiguration(it)) }
            settings.merge(newConfig)
            settings.save()
        }
    }

    fun register() = apply {
        PylonRegistry.ITEMS.register(this)
    }

    override fun getKey(): NamespacedKey = key

    override fun equals(other: Any?): Boolean = key == (other as? PylonItemSchema)?.key

    override fun hashCode(): Int = key.hashCode()

    companion object {
        private val itemsDir: Path = pluginInstance.dataPath.resolve("settings/item")

        init {
            itemsDir.createDirectories()
        }

        fun getItemFile(key: NamespacedKey): Path {
            return itemsDir.resolve(keyPath(key)).also { path ->
                path.parent.createDirectories()
            }
        }

        private fun keyPath(key: NamespacedKey) = "${key.namespace}/${key.key}.yml"
    }
}