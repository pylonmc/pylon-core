@file:JvmName("ItemConfig")

package io.github.pylonmc.pylon.core.config

import de.tr7zw.changeme.nbtapi.NBT
import io.github.pylonmc.pylon.core.item.customMiniMessage
import io.github.pylonmc.pylon.core.util.get
import io.github.pylonmc.pylon.core.util.nbt.SnbtNode
import io.github.pylonmc.pylon.core.util.nbt.parseSnbt
import io.github.pylonmc.pylon.core.util.nbt.snbtToYaml
import io.github.pylonmc.pylon.core.util.nbt.yamlToSnbt
import io.github.pylonmc.pylon.core.util.scalarValue
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer
import org.bukkit.inventory.ItemStack
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.inspector.TagInspector
import org.yaml.snakeyaml.nodes.*
import org.yaml.snakeyaml.representer.Representer
import java.io.Reader
import java.io.Writer

fun loadItemFromYaml(node: MappingNode): ItemStack {
    val item = yamlToSnbt(node)
    val miniMessagePaths = findMiniMessagePaths(node).toSet()
    val replacedComponents = replaceMiniMessage(item, emptyList(), miniMessagePaths)
    return NBT.itemStackFromNBT(NBT.parseNBT(replacedComponents.toString()))!!
}

fun loadItemFromYaml(reader: Reader): ItemStack {
    val fileNode = yamlParser.compose(reader)
    val itemNode = (fileNode as? MappingNode)?.get("item") as? MappingNode ?: error("Item not found")
    return loadItemFromYaml(itemNode)
}

fun saveItemToYaml(item: ItemStack, writer: Writer) {
    val nbt = NBT.itemStackToNBT(item)
    var snbt = parseSnbt(nbt.toString()) as SnbtNode.Compound
    snbt = SnbtNode.Compound(snbt.filterKeys { it != "count" })
    var yaml = snbtToYaml(snbt)
    yaml = MappingNode(
        Tag.MAP,
        listOf(
            NodeTuple(
                ScalarNode(Tag.STR, "item", null, null, DumperOptions.ScalarStyle.PLAIN),
                yaml,
            ),
        ),
        DumperOptions.FlowStyle.BLOCK
    )
    yamlDumper.serialize(yaml, writer)
}

private val yamlParser = Yaml(LoaderOptions().apply { tagInspector = TagInspector { _ -> true } })
private val yamlDumper = run {
    val options = DumperOptions()
    options.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
    options.defaultScalarStyle = DumperOptions.ScalarStyle.PLAIN
    Yaml(Representer(options), options)
}

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
