package io.github.pylonmc.pylon.core.util.nbt

sealed interface SnbtNode {

    val type: Type

    data class Boolean(val value: kotlin.Boolean) : SnbtNode {

        override val type = Type.BOOLEAN

        override fun toString() = value.toString()
    }

    data class Byte(val value: kotlin.Byte) : SnbtNode {

        override val type = Type.BYTE

        override fun toString() = value.toString() + "b"
    }

    data class Short(val value: kotlin.Short) : SnbtNode {

        override val type = Type.SHORT

        override fun toString() = value.toString() + "s"
    }

    data class Int(val value: kotlin.Int) : SnbtNode {

        override val type = Type.INT

        override fun toString() = value.toString()
    }

    data class Long(val value: kotlin.Long) : SnbtNode {

        override val type = Type.LONG

        override fun toString() = value.toString() + "l"
    }

    data class Float(val value: kotlin.Float) : SnbtNode {

        override val type = Type.FLOAT

        override fun toString() = value.toString() + "f"
    }

    data class Double(val value: kotlin.Double) : SnbtNode {

        override val type = Type.DOUBLE

        override fun toString() = value.toString()
    }

    data class String(val value: kotlin.String) : SnbtNode {

        override val type = Type.STRING

        override fun toString() = stringToString(value)
    }

    data class List(val value: kotlin.collections.List<SnbtNode>) : SnbtNode, kotlin.collections.List<SnbtNode> by value {

        override val type = Type.LIST

        override fun toString() = value.joinToString(separator = ", ", prefix = "[", postfix = "]")
    }

    data class Compound(val value: Map<kotlin.String, SnbtNode>) : SnbtNode, Map<kotlin.String, SnbtNode> by value {

        override val type = Type.COMPOUND

        override fun toString() = value.entries.joinToString(separator = ", ", prefix = "{", postfix = "}") {
            "${stringToString(it.key)}: ${it.value}"
        }
    }

    data class ByteArray(val value: kotlin.collections.List<Byte>) : SnbtNode {

        override val type = Type.BYTE_ARRAY

        override fun toString() = value.joinToString(separator = ", ", prefix = "[B;", postfix = "]")
    }

    data class IntArray(val value: kotlin.collections.List<Int>) : SnbtNode {

        override val type = Type.INT_ARRAY

        override fun toString() = value.joinToString(separator = ", ", prefix = "[I;", postfix = "]")
    }

    data class LongArray(val value: kotlin.collections.List<Long>) : SnbtNode {

        override val type = Type.LONG_ARRAY

        override fun toString() = value.joinToString(separator = ", ", prefix = "[L;", postfix = "]")
    }

    enum class Type {
        BOOLEAN,
        BYTE,
        SHORT,
        INT,
        LONG,
        FLOAT,
        DOUBLE,
        STRING,
        LIST,
        COMPOUND,
        BYTE_ARRAY,
        INT_ARRAY,
        LONG_ARRAY
    }
}

private fun stringToString(value: String): String {
    return "\"${value.replace("\"", "\\\"").replace("'", "\\'")}\""
}