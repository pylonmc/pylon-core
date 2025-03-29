package io.github.pylonmc.pylon.core.util.nbt

import org.yaml.snakeyaml.nodes.Tag

sealed interface SnbtNode {

    val type: Type

    sealed interface Scalar<T> : SnbtNode {
        val value: T
    }

    data class Boolean(override val value: kotlin.Boolean) : Scalar<kotlin.Boolean> {

        override val type = Type.BOOLEAN

        override fun toString() = value.toString()
    }

    data class Byte(override val value: kotlin.Byte) : Scalar<kotlin.Byte> {

        override val type = Type.BYTE

        override fun toString() = value.toString() + "b"
    }

    data class Short(override val value: kotlin.Short) : Scalar<kotlin.Short> {

        override val type = Type.SHORT

        override fun toString() = value.toString() + "s"
    }

    data class Int(override val value: kotlin.Int) : Scalar<kotlin.Int> {

        override val type = Type.INT

        override fun toString() = value.toString()
    }

    data class Long(override val value: kotlin.Long) : Scalar<kotlin.Long> {

        override val type = Type.LONG

        override fun toString() = value.toString() + "l"
    }

    data class Float(override val value: kotlin.Float) : Scalar<kotlin.Float> {

        override val type = Type.FLOAT

        override fun toString() = value.toString() + "f"
    }

    data class Double(override val value: kotlin.Double) : Scalar<kotlin.Double> {

        override val type = Type.DOUBLE

        override fun toString() = value.toString() + "d"
    }

    data class String(override val value: kotlin.String) : Scalar<kotlin.String> {

        override val type = Type.STRING

        override fun toString() = stringToString(value)
    }

    sealed interface Sequence<out N : SnbtNode> : SnbtNode, kotlin.collections.List<N>

    data class List(val value: kotlin.collections.List<SnbtNode>) : Sequence<SnbtNode>, kotlin.collections.List<SnbtNode> by value {

        override val type = Type.LIST

        override fun toString() = value.joinToString(separator = ", ", prefix = "[", postfix = "]")
    }

    data class Compound(val value: Map<kotlin.String, SnbtNode>) : SnbtNode, Map<kotlin.String, SnbtNode> by value {

        override val type = Type.COMPOUND

        override fun toString() = value.entries.joinToString(separator = ", ", prefix = "{", postfix = "}") {
            "${stringToString(it.key)}: ${it.value}"
        }
    }

    data class ByteArray(val value: kotlin.collections.List<Byte>) : Sequence<Byte>, kotlin.collections.List<Byte> by value {

        override val type = Type.BYTE_ARRAY

        override fun toString() = value.joinToString(separator = ", ", prefix = "[B;", postfix = "]")
    }

    data class IntArray(val value: kotlin.collections.List<Int>) : Sequence<Int>, kotlin.collections.List<Int> by value {

        override val type = Type.INT_ARRAY

        override fun toString() = value.joinToString(separator = ", ", prefix = "[I;", postfix = "]")
    }

    data class LongArray(val value: kotlin.collections.List<Long>) : Sequence<Long>, kotlin.collections.List<Long> by value {

        override val type = Type.LONG_ARRAY

        override fun toString() = value.joinToString(separator = ", ", prefix = "[L;", postfix = "]")
    }

    enum class Type(ymlTag: kotlin.String) {
        BOOLEAN("boolean"),
        BYTE("byte"),
        SHORT("short"),
        INT("int"),
        LONG("long"),
        FLOAT("float"),
        DOUBLE("double"),
        STRING("string"),
        LIST("list"),
        COMPOUND("compound"),
        BYTE_ARRAY("bytearray"),
        INT_ARRAY("intarray"),
        LONG_ARRAY("longarray");

        val ymlTag = Tag("!$ymlTag")
    }
}

private fun stringToString(value: String): String {
    return "\"${value.replace("\"", "\\\"").replace("'", "\\'")}\""
}