package io.github.pylonmc.pylon.core.block

import io.github.pylonmc.pylon.core.AlreadyRegisteredException
import io.github.pylonmc.pylon.core.MissingLoadConstructorException
import io.github.pylonmc.pylon.core.MissingPlaceConstructorException
import io.github.pylonmc.pylon.core.NotRegisteredException
import io.github.pylonmc.pylon.core.addon.PylonAddon
import io.github.pylonmc.pylon.core.state.StateReader
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import java.lang.reflect.Constructor

open class PylonBlockSchema(
    private val idWithoutNamespace: String,
    val material: Material,
    blockClass: Class<PylonBlock<PylonBlockSchema>>,
) {
    private var addon: PylonAddon? = null
    val id: NamespacedKey
        get() {
            if (addon == null) {
                throw NotRegisteredException(idWithoutNamespace)
            }
            return NamespacedKey(addon!!.javaPlugin, idWithoutNamespace)
        }

    internal val placeConstructor: Constructor<PylonBlock<PylonBlockSchema>> = try {
        val c = blockClass.getConstructor(PylonBlockSchema::class.java)
        c.isAccessible = true
        c
    } catch (e: NoSuchMethodException) {
        throw MissingPlaceConstructorException(idWithoutNamespace)
    }

    internal val loadConstructor: Constructor<PylonBlock<PylonBlockSchema>> = try {
        val c = blockClass.getConstructor(StateReader::class.java, Block::class.java)
        c.isAccessible = true
        c
    } catch (e: NoSuchMethodException) {
        throw MissingLoadConstructorException(idWithoutNamespace)
    }

    fun register(addon: PylonAddon) {
        val newId = NamespacedKey(addon.javaPlugin, idWithoutNamespace)
        if (isSchemaRegistered(newId)) {
            throw AlreadyRegisteredException(newId.toString())
        }
        this.addon = addon
        schemas[newId] = this
    }

    companion object {
        private var schemas: MutableMap<NamespacedKey, PylonBlockSchema> = HashMap()

        fun getSchema(id: NamespacedKey): PylonBlockSchema? = schemas[id]

        fun getRegisteredSchemas(): MutableCollection<PylonBlockSchema> = schemas.values

        fun getRegisteredSchemasByAddon(addon: PylonAddon): List<PylonBlockSchema>
            = schemas.values.filter { it.addon == addon }

        fun isSchemaRegistered(id: NamespacedKey): Boolean = schemas.containsKey(id)

        fun reloadSchema(id: NamespacedKey, newSchema: PylonBlockSchema) {
            if (!schemas.containsKey(id)) {
                throw NotRegisteredException(id.key)
            }
            schemas[id] = newSchema
        }
    }
}