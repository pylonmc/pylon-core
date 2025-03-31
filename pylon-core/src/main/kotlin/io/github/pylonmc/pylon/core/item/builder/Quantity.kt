package io.github.pylonmc.pylon.core.item.builder

object Quantity {
    const val NONE = ""
    const val BLOCKS = "<#1eaa56>blocks</#1eaa56>"
    const val SECONDS = "<#c9c786>s</#c9c786>"
    const val HEARTS = "<#db3b43>\u2764</#db3b43>"

    @JvmStatic
    fun byName(name: String): String? {
        return when (name.lowercase()) {
            "blocks" -> BLOCKS
            "seconds" -> SECONDS
            "hearts" -> HEARTS
            else -> null
        }
    }
}