package io.github.pylonmc.pylon.core.util.nbt

sealed interface SnbtNode {

    val type: SnbtNodeType

    data class Boolean(val value: kotlin.Boolean) : SnbtNode {
        override val type = SnbtNodeType.BOOLEAN
    }

    data class Byte(val value: kotlin.Byte) : SnbtNode {
        override val type = SnbtNodeType.BYTE
    }

    data class Short(val value: kotlin.Short) : SnbtNode {
        override val type = SnbtNodeType.SHORT
    }

    data class Int(val value: kotlin.Int) : SnbtNode {
        override val type = SnbtNodeType.INT
    }

    data class Long(val value: kotlin.Long) : SnbtNode {
        override val type = SnbtNodeType.LONG
    }

    data class Float(val value: kotlin.Float) : SnbtNode {
        override val type = SnbtNodeType.FLOAT
    }

    data class Double(val value: kotlin.Double) : SnbtNode {
        override val type = SnbtNodeType.DOUBLE
    }

    data class String(val value: kotlin.String) : SnbtNode {
        override val type = SnbtNodeType.STRING
    }

    data class List(val value: kotlin.collections.List<SnbtNode>) : SnbtNode {
        override val type = SnbtNodeType.LIST
    }

    data class Compound(val value: Map<kotlin.String, SnbtNode>) : SnbtNode {
        override val type = SnbtNodeType.COMPOUND
    }

    data class ByteArray(val value: kotlin.ByteArray) : SnbtNode {

        override val type = SnbtNodeType.BYTE_ARRAY

        override fun equals(other: Any?): kotlin.Boolean {
            return other === this || (other is ByteArray && value.contentEquals(other.value))
        }

        override fun hashCode(): kotlin.Int {
            return value.contentHashCode()
        }
    }

    data class IntArray(val value: kotlin.IntArray) : SnbtNode {

        override val type = SnbtNodeType.INT_ARRAY

        override fun equals(other: Any?): kotlin.Boolean {
            return other === this || (other is IntArray && value.contentEquals(other.value))
        }

        override fun hashCode(): kotlin.Int {
            return value.contentHashCode()
        }
    }

    data class LongArray(val value: kotlin.LongArray) : SnbtNode {

        override val type = SnbtNodeType.LONG_ARRAY

        override fun equals(other: Any?): kotlin.Boolean {
            return other === this || (other is LongArray && value.contentEquals(other.value))
        }

        override fun hashCode(): kotlin.Int {
            return value.contentHashCode()
        }
    }
}