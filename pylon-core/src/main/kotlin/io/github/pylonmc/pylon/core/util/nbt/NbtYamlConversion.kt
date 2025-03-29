@file:JvmName("NbtYamlConversion")

package io.github.pylonmc.pylon.core.util.nbt

import io.github.pylonmc.pylon.core.util.scalarValue
import org.yaml.snakeyaml.DumperOptions
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

private fun getNodeType(node: Node): SnbtNode.Type {
    val tag = node.tag
    if (!tag.startsWith(Tag.PREFIX)) {
        return SnbtNode.Type.entries.find { it.ymlTag == tag } ?: SnbtNode.Type.STRING
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

fun snbtToYaml(node: SnbtNode): Node {
    val tag = getTag(node)
    return when (node) {
        is SnbtNode.Scalar<*> -> ScalarNode(
            tag,
            node.value.toString(),
            null,
            null,
            if (node is SnbtNode.String) DumperOptions.ScalarStyle.DOUBLE_QUOTED else DumperOptions.ScalarStyle.PLAIN
        )

        is SnbtNode.List,
        is SnbtNode.ByteArray,
        is SnbtNode.IntArray,
        is SnbtNode.LongArray -> SequenceNode(
            tag,
            node.map(::snbtToYaml),
            DumperOptions.FlowStyle.AUTO
        )

        is SnbtNode.Compound -> MappingNode(
            tag,
            node.map { (key, value) ->
                NodeTuple(ScalarNode(Tag.STR, key, null, null, DumperOptions.ScalarStyle.DOUBLE_QUOTED), snbtToYaml(value))
            },
            DumperOptions.FlowStyle.AUTO
        )
    }
}

private fun getTag(node: SnbtNode): Tag {
    return when (node) {
        is SnbtNode.Byte -> SnbtNode.Type.BYTE.ymlTag
        is SnbtNode.Boolean -> Tag.BOOL
        is SnbtNode.Short -> SnbtNode.Type.SHORT.ymlTag
        is SnbtNode.Int -> Tag.INT
        is SnbtNode.Long -> SnbtNode.Type.LONG.ymlTag
        is SnbtNode.Float -> SnbtNode.Type.FLOAT.ymlTag
        is SnbtNode.Double -> Tag.FLOAT
        is SnbtNode.String -> Tag.STR
        // @formatter:off
        is SnbtNode.List -> if (node.all {
            it.type == SnbtNode.Type.BYTE || it.type == SnbtNode.Type.INT || it.type == SnbtNode.Type.LONG
        }) {
            SnbtNode.Type.LIST.ymlTag // override type inference
        } else {
            Tag.SEQ
        }
        // @formatter:on
        is SnbtNode.Compound -> Tag.MAP
        is SnbtNode.ByteArray -> Tag.SEQ
        is SnbtNode.IntArray -> Tag.SEQ
        is SnbtNode.LongArray -> Tag.SEQ
    }
}