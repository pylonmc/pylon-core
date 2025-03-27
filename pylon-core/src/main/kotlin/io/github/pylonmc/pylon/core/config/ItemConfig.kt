@file:JvmName("ItemConfig")

package io.github.pylonmc.pylon.core.config

import de.tr7zw.changeme.nbtapi.NBT
import io.github.pylonmc.pylon.core.item.customMiniMessage
import io.github.pylonmc.pylon.core.util.get
import io.github.pylonmc.pylon.core.util.nbt.SnbtNode
import io.github.pylonmc.pylon.core.util.nbt.yamlToSnbt
import io.github.pylonmc.pylon.core.util.scalarValue
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.inspector.TagInspector
import org.yaml.snakeyaml.nodes.*
import java.io.InputStream

fun loadItemFromYml(node: MappingNode): ItemStack {
    val typeString = node["type"]?.scalarValue ?: error("Item type not specified")
    val type = Material.matchMaterial(typeString) ?: error("Invalid item type: $typeString")
    val item = ItemStack(type)
    val componentsNode = node["components"] ?: MappingNode(Tag.MAP, mutableListOf(), DumperOptions.FlowStyle.AUTO)
    val components = yamlToSnbt(componentsNode)
    val miniMessagePaths = findMiniMessagePaths(componentsNode).toSet()
    val replacedComponents = replaceMiniMessage(components, emptyList(), miniMessagePaths)
    NBT.modifyComponents(item) { nbt ->
        nbt.mergeCompound(NBT.parseNBT(replacedComponents.toString()))
    }
    return item
}

fun loadItemFromYml(stream: InputStream): ItemStack {
    val fileNode = yamlParser.compose(stream.reader())
    val itemNode = (fileNode as? MappingNode)?.get("item") as? MappingNode ?: error("Item not found")
    return loadItemFromYml(itemNode)
}

private val yamlParser = Yaml(LoaderOptions().apply { tagInspector = TagInspector { _ -> true } })

private fun findMiniMessagePaths(node: Node, path: List<String> = emptyList()): List<List<String>> {
    return when (node) {
        is SequenceNode -> node.value.flatMapIndexed { index, value ->
            findMiniMessagePaths(value, path + index.toString())
        }

        is MappingNode -> node.value.flatMap { tuple ->
            findMiniMessagePaths(tuple.valueNode, path + tuple.keyNode.scalarValue)
        }

        is ScalarNode if node.tag.value == "!mm" -> listOf(path)

        else -> emptyList()
    }
}

private fun replaceMiniMessage(node: SnbtNode, path: List<String>, mmPaths: Set<List<String>>): SnbtNode {
    return when (node) {
        is SnbtNode.List -> {
            val newList = node.mapIndexed { index, value ->
                replaceMiniMessage(value, path + index.toString(), mmPaths)
            }
            SnbtNode.List(newList)
        }

        is SnbtNode.Compound -> {
            val newCompound = node.mapValues { (key, value) ->
                replaceMiniMessage(value, path + key, mmPaths)
            }
            SnbtNode.Compound(newCompound)
        }

        is SnbtNode.String if path in mmPaths -> SnbtNode.String(
            JSONComponentSerializer.json().serialize(customMiniMessage.deserialize(node.value))
        )

        else -> node
    }
}
