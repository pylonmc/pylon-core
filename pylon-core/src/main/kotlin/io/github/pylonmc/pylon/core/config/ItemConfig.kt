@file:JvmName("ItemConfig")

package io.github.pylonmc.pylon.core.config

import de.tr7zw.changeme.nbtapi.NBT
import io.github.pylonmc.pylon.core.util.get
import io.github.pylonmc.pylon.core.util.nbt.yamlToSnbt
import io.github.pylonmc.pylon.core.util.scalarValue
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.inspector.TagInspector
import org.yaml.snakeyaml.nodes.MappingNode
import java.io.InputStream

fun loadItemFromYml(node: MappingNode): ItemStack {
    val typeString = node["type"]?.scalarValue ?: error("Item type not specified")
    val type = Material.matchMaterial(typeString) ?: error("Invalid item type: $typeString")
    val item = ItemStack(type)
    val components = node["components"]?.let(::yamlToSnbt)?.toString() ?: "{}"
    println(components)
    NBT.modifyComponents(item) { nbt ->
        nbt.mergeCompound(NBT.parseNBT(components))
    }
    return item
}

fun loadItemFromYml(stream: InputStream): ItemStack {
    val fileNode = yamlParser.compose(stream.reader())
    val itemNode = (fileNode as? MappingNode)?.get("item") as? MappingNode ?: error("Item not found")
    return loadItemFromYml(itemNode)
}

private val yamlParser = Yaml(LoaderOptions().apply { tagInspector = TagInspector { _ -> true } })