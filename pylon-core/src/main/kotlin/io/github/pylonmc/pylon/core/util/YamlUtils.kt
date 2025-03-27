@file:JvmSynthetic

package io.github.pylonmc.pylon.core.util

import org.yaml.snakeyaml.nodes.MappingNode
import org.yaml.snakeyaml.nodes.Node
import org.yaml.snakeyaml.nodes.ScalarNode

val Node.scalarValue: String
    get() = (this as ScalarNode).value

operator fun MappingNode.get(key: String): Node? {
    return value.find { it.keyNode.scalarValue == key }?.valueNode
}