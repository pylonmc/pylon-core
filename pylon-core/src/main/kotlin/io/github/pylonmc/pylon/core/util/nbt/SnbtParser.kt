@file:JvmName("SnbtParser")

package io.github.pylonmc.pylon.core.util.nbt

import org.intellij.lang.annotations.Language

fun parseSnbt(string: String): SnbtNode {
    val tokens = lex(string).filter { it.type != Token.Type.WHITESPACE }
    try {
        return Parser(tokens).parseNode()
    } catch (e: ParseException) {
        throw ParseException("Error at position ${e.position} (${string.substring(0, e.position)}): ${e.message}", e.position)
    }
}

private fun lex(string: String): List<Token> {
    val tokens = mutableListOf<Token>()
    var next = StringBuilder(string)
    while (next.isNotEmpty()) {
        val matched = TokenMatcher.matchers.map { it to it.match(next) }.filter { it.second > 0 }
        if (matched.isEmpty()) {
            throw IllegalArgumentException("Unexpected character: ${next.first()}")
        }
        val (matcher, length) = matched.maxBy { it.second }
        tokens.add(Token(matcher.type, next.substring(0, length)))
        next.delete(0, length)
    }
    return tokens
}

private sealed interface TokenMatcher {

    fun match(next: CharSequence): Int

    val type: Token.Type

    class Regex(override val type: Token.Type, @Language("RegExp") regex: String) : TokenMatcher {
        private val regex = "^$regex".toRegex()
        override fun match(next: CharSequence): Int {
            return regex.find(next)?.value?.length ?: 0
        }
    }

    class Keyword(override val type: Token.Type, private val literal: String) : TokenMatcher {
        override fun match(next: CharSequence): Int {
            return if (next.startsWith(literal)) literal.length else 0
        }
    }

    object StringLiteral : TokenMatcher {
        override val type = Token.Type.STRING
        override fun match(next: CharSequence): Int {
            val quote = next.first()
            if (quote != '"' && quote != '\'') return 0
            var escaped = false
            var i = 1
            while (i < next.length) {
                val char = next[i]
                if (char == quote && !escaped) {
                    return i + 1
                }
                escaped = char == '\\' && !escaped
                i++
            }
            return 0
        }
    }

    companion object {
        val matchers = listOf(
            Regex(Token.Type.BYTE, "[+-]?[0-9]+[Bb]"),
            Keyword(Token.Type.TRUE, "true"),
            Keyword(Token.Type.FALSE, "false"),
            Regex(Token.Type.SHORT, "[+-]?[0-9]+[Ss]"),
            Regex(Token.Type.INT, "[+-]?[0-9]+"),
            Regex(Token.Type.LONG, "[+-]?[0-9]+[Ll]"),
            Regex(Token.Type.FLOAT, "[+-]?[0-9]+\\.[0-9]+[Ff]"),
            Regex(Token.Type.DOUBLE, "[+-]?[0-9]+\\.[0-9]+[Dd]?"),
            StringLiteral,
            Regex(Token.Type.NAKED_STRING, """[a-zA-Z0-9_\-+.]+"""),
            Keyword(Token.Type.LIST_START, "["),
            Keyword(Token.Type.LIST_END, "]"),
            Keyword(Token.Type.COMPOUND_START, "{"),
            Keyword(Token.Type.COMPOUND_END, "}"),
            Keyword(Token.Type.BYTE_ARRAY_START, "[B;"),
            Keyword(Token.Type.INT_ARRAY_START, "[I;"),
            Keyword(Token.Type.LONG_ARRAY_START, "[L;"),
            Keyword(Token.Type.COMMA, ","),
            Keyword(Token.Type.COLON, ":"),
            Regex(Token.Type.WHITESPACE, "\\s+"),
        )
    }
}

private data class Token(val type: Type, val content: String) {
    enum class Type {
        BYTE,
        TRUE,
        FALSE,
        SHORT,
        INT,
        LONG,
        FLOAT,
        DOUBLE,
        STRING,
        NAKED_STRING,
        LIST_START,
        LIST_END,
        COMPOUND_START,
        COMPOUND_END,
        BYTE_ARRAY_START,
        INT_ARRAY_START,
        LONG_ARRAY_START,
        COMMA,
        COLON,
        WHITESPACE,
    }
}

private class Parser(private val tokens: List<Token>) {

    var index = 0

    fun parseNode(): SnbtNode {
        return oneOf(
            ::parseByte,
            ::parseBoolean,
            ::parseShort,
            ::parseInt,
            ::parseLong,
            ::parseFloat,
            ::parseDouble,
            ::parseString,
            ::parseList,
            ::parseCompound,
            ::parseByteArray,
            ::parseIntArray,
            ::parseLongArray,
        )
    }

    fun parseByte(): SnbtNode.Byte {
        return SnbtNode.Byte(consume(Token.Type.BYTE).content.dropLast(1).toByte())
    }

    fun parseBoolean(): SnbtNode.Boolean {
        return SnbtNode.Boolean(consume(Token.Type.TRUE, Token.Type.FALSE).type == Token.Type.TRUE)
    }

    fun parseShort(): SnbtNode.Short {
        return SnbtNode.Short(consume(Token.Type.SHORT).content.dropLast(1).toShort())
    }

    fun parseInt(): SnbtNode.Int {
        return SnbtNode.Int(consume(Token.Type.INT).content.toInt())
    }

    fun parseLong(): SnbtNode.Long {
        return SnbtNode.Long(consume(Token.Type.LONG).content.dropLast(1).toLong())
    }

    fun parseFloat(): SnbtNode.Float {
        return SnbtNode.Float(consume(Token.Type.FLOAT).content.dropLast(1).toFloat())
    }

    fun parseDouble(): SnbtNode.Double {
        var double = consume(Token.Type.DOUBLE).content
        if (double.endsWith('D', ignoreCase = true)) {
            double = double.dropLast(1)
        }
        return SnbtNode.Double(double.toDouble())
    }

    fun parseString(): SnbtNode.String {
        val token = consume(Token.Type.STRING, Token.Type.NAKED_STRING).content
        val value = if (token.startsWith('"') || token.startsWith('\'')) {
            token.drop(1).dropLast(1)
        } else {
            token
        }
        return SnbtNode.String(value)
    }

    fun parseList(): SnbtNode.List {
        return SnbtNode.List(parseCommaDelimited(Token.Type.LIST_START, Token.Type.LIST_END, ::parseNode))
    }

    fun parseCompound(): SnbtNode.Compound {
        val pairs = parseCommaDelimited(Token.Type.COMPOUND_START, Token.Type.COMPOUND_END) {
            val key = parseString().value
            consume(Token.Type.COLON)
            val value = parseNode()
            key to value
        }
        return SnbtNode.Compound(pairs.toMap())
    }

    fun parseByteArray(): SnbtNode.ByteArray {
        return SnbtNode.ByteArray(parseCommaDelimited(Token.Type.BYTE_ARRAY_START, Token.Type.LIST_END, ::parseByte))
    }

    fun parseIntArray(): SnbtNode.IntArray {
        return SnbtNode.IntArray(parseCommaDelimited(Token.Type.INT_ARRAY_START, Token.Type.LIST_END, ::parseInt))
    }

    fun parseLongArray(): SnbtNode.LongArray {
        return SnbtNode.LongArray(parseCommaDelimited(Token.Type.LONG_ARRAY_START, Token.Type.LIST_END, ::parseLong))
    }

    fun tryConsume(vararg types: Token.Type): Token? {
        val token = tokens[index]
        if (token.type in types) {
            index++
            return token
        }
        return null
    }

    fun consume(vararg types: Token.Type): Token {
        return tryConsume(*types) ?: throw ParseException(
            "Expected one of ${types.joinToString(", ") { it.name }} but found ${tokens[index].type.name}",
            index
        )
    }

    fun <T> oneOf(vararg parsers: () -> T): T {
        val errors = mutableListOf<ParseException>()
        val startIndex = index
        for (parser in parsers) {
            try {
                return parser()
            } catch (e: ParseException) {
                errors.add(e)
                index = startIndex
            }
        }
        throw errors.maxBy { it.position }
    }

    inline fun <T> parseCommaDelimited(start: Token.Type, end: Token.Type, parser: () -> T): List<T> {
        val values = mutableListOf<T>()
        consume(start)
        while (true) {
            if (tryConsume(end) != null) break
            values.add(parser())
            if (tryConsume(end) != null) break
            consume(Token.Type.COMMA)
        }
        return values
    }
}

class ParseException(message: String, val position: Int) : RuntimeException(message)