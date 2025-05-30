package io.github.pylonmc.pylon.core.i18n.wrapping

class TextWrapper(private val limit: Int) {

    fun wrap(text: String): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            if (currentLine.length + word.length + 1 > limit) {
                currentLine.append(' ')
                lines.add(currentLine.toString())
                currentLine = StringBuilder()
            }
            if (currentLine.isNotEmpty()) {
                currentLine.append(" ")
            }
            currentLine.append(word)
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine.toString())
        }
        return lines
    }
}