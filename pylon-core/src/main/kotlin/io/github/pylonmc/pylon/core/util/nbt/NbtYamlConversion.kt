@file:JvmName("NbtYamlConversion")

package io.github.pylonmc.pylon.core.util.nbt

import de.tr7zw.changeme.nbtapi.NBT
import de.tr7zw.changeme.nbtapi.NBTType
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT
import org.yaml.snakeyaml.nodes.MappingNode
import org.yaml.snakeyaml.nodes.Node
import org.yaml.snakeyaml.nodes.ScalarNode
import org.yaml.snakeyaml.nodes.SequenceNode
import org.yaml.snakeyaml.nodes.Tag

fun yamlToSnbt(node: Node): SnbtNode {
    return when (getNodeType(node)) {
        SnbtNodeType.BOOLEAN -> SnbtNode.Boolean(node.scalarValue.toBooleanStrict())
        SnbtNodeType.BYTE -> SnbtNode.Byte(node.scalarValue.toByte())
        SnbtNodeType.SHORT -> SnbtNode.Short(node.scalarValue.toShort())
        SnbtNodeType.INT -> SnbtNode.Int(node.scalarValue.toInt())
        SnbtNodeType.LONG -> SnbtNode.Long(node.scalarValue.toLong())
        SnbtNodeType.FLOAT -> SnbtNode.Float(node.scalarValue.toFloat())
        SnbtNodeType.DOUBLE -> SnbtNode.Double(node.scalarValue.toDouble())
        SnbtNodeType.STRING -> SnbtNode.String(node.scalarValue)
        SnbtNodeType.LIST -> SnbtNode.List(node.sequenceValue.map(::yamlToSnbt))
        SnbtNodeType.COMPOUND -> SnbtNode.Compound(
            (node as MappingNode).value.associate { it.keyNode.scalarValue to yamlToSnbt(it.valueNode) }
        )
        SnbtNodeType.BYTE_ARRAY -> SnbtNode.ByteArray(node.sequenceValue.map { it.scalarValue.toByte() }.toByteArray())
        SnbtNodeType.INT_ARRAY -> SnbtNode.IntArray(node.sequenceValue.map { it.scalarValue.toInt() }.toIntArray())
        SnbtNodeType.LONG_ARRAY -> SnbtNode.LongArray(node.sequenceValue.map { it.scalarValue.toLong() }.toLongArray())
    }
}

private val Node.scalarValue: String
    get() = (this as ScalarNode).value

private val Node.sequenceValue: List<Node>
    get() = (this as SequenceNode).value

private val snbtTagTypes = mapOf(
    "boolean" to SnbtNodeType.BOOLEAN,
    "byte" to SnbtNodeType.BYTE,
    "short" to SnbtNodeType.SHORT,
    "int" to SnbtNodeType.INT,
    "long" to SnbtNodeType.LONG,
    "float" to SnbtNodeType.FLOAT,
    "double" to SnbtNodeType.DOUBLE,
    "string" to SnbtNodeType.STRING,
    "list" to SnbtNodeType.LIST,
    "compound" to SnbtNodeType.COMPOUND,
    "bytearray" to SnbtNodeType.BYTE_ARRAY,
    "intarray" to SnbtNodeType.INT_ARRAY,
    "longarray" to SnbtNodeType.LONG_ARRAY
)

private fun getNodeType(node: Node): SnbtNodeType {
    val tag = node.tag
    if (!tag.startsWith(Tag.PREFIX)) {
        val value = tag.value.removePrefix("!")
        return snbtTagTypes[tag.value.removePrefix("!")] ?: SnbtNodeType.STRING
    }

    // yaay type inference my favorite
    return when (node) {
        is MappingNode -> SnbtNodeType.COMPOUND
        is SequenceNode -> {
            val subtypes = node.value.map(::getNodeType)
            when {
                subtypes.all { it == SnbtNodeType.BYTE } -> SnbtNodeType.BYTE_ARRAY
                subtypes.all { it == SnbtNodeType.INT } -> SnbtNodeType.INT_ARRAY
                subtypes.all { it == SnbtNodeType.LONG } -> SnbtNodeType.LONG_ARRAY
                else -> SnbtNodeType.LIST
            }
        }

        is ScalarNode -> when (tag) {
            Tag.INT -> SnbtNodeType.INT
            Tag.FLOAT -> SnbtNodeType.DOUBLE
            Tag.BOOL -> SnbtNodeType.BOOLEAN
            else -> throw IllegalArgumentException("Unknown type: $node")
        }

        else -> throw IllegalArgumentException("$node cannot be deserialized")
    }
}