@file:JvmName("NbtYamlConversion")

package io.github.pylonmc.pylon.core.util.nbt

import io.github.pylonmc.pylon.core.util.scalarValue
import org.yaml.snakeyaml.nodes.*

fun yamlToSnbt(node: Node): SnbtNode {
    return when (getNodeType(node)) {
        SnbtNode.Type.BOOLEAN -> SnbtNode.Boolean(node.scalarValue.toBooleanStrict())
        SnbtNode.Type.BYTE -> SnbtNode.Byte(node.scalarValue.toByte())
        SnbtNode.Type.SHORT -> SnbtNode.Short(node.scalarValue.toShort())
        SnbtNode.Type.INT -> SnbtNode.Int(node.scalarValue.toInt())
        SnbtNode.Type.LONG -> SnbtNode.Long(node.scalarValue.toLong())
        SnbtNode.Type.FLOAT -> SnbtNode.Float(node.scalarValue.toFloat())
        SnbtNode.Type.DOUBLE -> SnbtNode.Double(node.scalarValue.toDouble())
        SnbtNode.Type.STRING -> SnbtNode.String(node.scalarValue)
        SnbtNode.Type.LIST -> SnbtNode.List(node.sequenceValue.map(::yamlToSnbt))
        SnbtNode.Type.COMPOUND -> SnbtNode.Compound(
            (node as MappingNode).value.associate { it.keyNode.scalarValue to yamlToSnbt(it.valueNode) }
        )
        SnbtNode.Type.BYTE_ARRAY -> SnbtNode.ByteArray(node.sequenceValue.map { SnbtNode.Byte(it.scalarValue.toByte()) })
        SnbtNode.Type.INT_ARRAY -> SnbtNode.IntArray(node.sequenceValue.map { SnbtNode.Int(it.scalarValue.toInt()) })
        SnbtNode.Type.LONG_ARRAY -> SnbtNode.LongArray(node.sequenceValue.map { SnbtNode.Long(it.scalarValue.toLong()) })
    }
}

private val Node.sequenceValue: List<Node>
    get() = (this as SequenceNode).value

private val snbtTagTypes = mapOf(
    "boolean" to SnbtNode.Type.BOOLEAN,
    "byte" to SnbtNode.Type.BYTE,
    "short" to SnbtNode.Type.SHORT,
    "int" to SnbtNode.Type.INT,
    "long" to SnbtNode.Type.LONG,
    "float" to SnbtNode.Type.FLOAT,
    "double" to SnbtNode.Type.DOUBLE,
    "string" to SnbtNode.Type.STRING,
    "list" to SnbtNode.Type.LIST,
    "compound" to SnbtNode.Type.COMPOUND,
    "bytearray" to SnbtNode.Type.BYTE_ARRAY,
    "intarray" to SnbtNode.Type.INT_ARRAY,
    "longarray" to SnbtNode.Type.LONG_ARRAY
)

private fun getNodeType(node: Node): SnbtNode.Type {
    val tag = node.tag
    if (!tag.startsWith(Tag.PREFIX)) {
        return snbtTagTypes[tag.value.removePrefix("!")] ?: SnbtNode.Type.STRING
    }

    // yaay type inference my favorite
    return when (node) {
        is MappingNode -> SnbtNode.Type.COMPOUND
        is SequenceNode -> {
            val subtypes = node.value.map(::getNodeType)
            val first = subtypes.firstOrNull()
            if (first != null && subtypes.any { it != first }) {
                throw IllegalArgumentException("Inconsistent types in list: $node")
            }
            when {
                subtypes.all { it == SnbtNode.Type.BYTE } -> SnbtNode.Type.BYTE_ARRAY
                subtypes.all { it == SnbtNode.Type.INT } -> SnbtNode.Type.INT_ARRAY
                subtypes.all { it == SnbtNode.Type.LONG } -> SnbtNode.Type.LONG_ARRAY
                else -> SnbtNode.Type.LIST
            }
        }

        is ScalarNode -> when (tag) {
            Tag.INT -> SnbtNode.Type.INT
            Tag.FLOAT -> SnbtNode.Type.DOUBLE
            Tag.BOOL -> SnbtNode.Type.BOOLEAN
            Tag.STR -> SnbtNode.Type.STRING
            else -> throw IllegalArgumentException("Unknown type: $node")
        }

        else -> throw IllegalArgumentException("$node cannot be deserialized")
    }
}