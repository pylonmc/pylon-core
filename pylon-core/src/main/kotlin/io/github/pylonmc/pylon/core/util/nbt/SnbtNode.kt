package io.github.pylonmc.pylon.core.util.nbt

sealed interface SnbtNode {

    val type: Type

    data class Boolean(val value: kotlin.Boolean) : SnbtNode {
        override val type = Type.BOOLEAN
    }

    data class Byte(val value: kotlin.Byte) : SnbtNode {
        override val type = Type.BYTE
    }

    data class Short(val value: kotlin.Short) : SnbtNode {
        override val type = Type.SHORT
    }

    data class Int(val value: kotlin.Int) : SnbtNode {
        override val type = Type.INT
    }

    data class Long(val value: kotlin.Long) : SnbtNode {
        override val type = Type.LONG
    }

    data class Float(val value: kotlin.Float) : SnbtNode {
        override val type = Type.FLOAT
    }

    data class Double(val value: kotlin.Double) : SnbtNode {
        override val type = Type.DOUBLE
    }

    data class String(val value: kotlin.String) : SnbtNode {
        override val type = Type.STRING
    }

    data class List(val value: kotlin.collections.List<SnbtNode>) : SnbtNode, kotlin.collections.List<SnbtNode> by value {
        override val type = Type.LIST
    }

    data class Compound(val value: Map<kotlin.String, SnbtNode>) : SnbtNode, Map<kotlin.String, SnbtNode> by value {
        override val type = Type.COMPOUND
    }

    data class ByteArray(val value: kotlin.ByteArray) : SnbtNode {

        override val type = Type.BYTE_ARRAY

        override fun equals(other: Any?): kotlin.Boolean {
            return other === this || (other is ByteArray && value.contentEquals(other.value))
        }

        override fun hashCode(): kotlin.Int {
            return value.contentHashCode()
        }
    }

    data class IntArray(val value: kotlin.IntArray) : SnbtNode {

        override val type = Type.INT_ARRAY

        override fun equals(other: Any?): kotlin.Boolean {
            return other === this || (other is IntArray && value.contentEquals(other.value))
        }

        override fun hashCode(): kotlin.Int {
            return value.contentHashCode()
        }
    }

    data class LongArray(val value: kotlin.LongArray) : SnbtNode {

        override val type = Type.LONG_ARRAY

        override fun equals(other: Any?): kotlin.Boolean {
            return other === this || (other is LongArray && value.contentEquals(other.value))
        }

        override fun hashCode(): kotlin.Int {
            return value.contentHashCode()
        }
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