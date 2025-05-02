package io.github.pylonmc.pylon.core.i18n.wrapping

import assertk.assertThat
import assertk.assertions.isEqualTo
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.minimessage.MiniMessage
import kotlin.test.Test

class WrapTest {

    private val wrapper = TextWrapper(8)

    @Test
    fun testWrapSimple() {
        checkWrap(
            "<green>Lorem ipsum dolor sit amet",
            """
                <green>Lorem 
                <green>ipsum 
                <green>dolor 
                <green>sit amet
            """.trimIndent()
        )
    }

    @Test
    fun testWrapMultiStyle() {
        checkWrap(
            "<yellow>Lorem <red>ipsum dolor sit amet",
            """
                <yellow>Lorem 
                <red>ipsum 
                <red>dolor 
                <red>sit amet
            """.trimIndent()
        )
    }

    private fun checkWrap(original: String, expected: String) {
        val mm = MiniMessage.miniMessage()
        val originalComponent = mm.deserialize(original)
        val expectedComponent = Component.join(JoinConfiguration.newlines(), expected.split('\n').map(mm::deserialize))
        val wrapped = wrap(originalComponent)
        assertThat(mm.serialize(wrapped)).isEqualTo(mm.serialize(expectedComponent))
    }

    private fun wrap(input: Component): Component {
        var repr = LineWrapEncoder.encode(input)
        repr = repr.copy(
            lines = repr.lines.flatMap(wrapper::wrap),
            styles = repr.styles
        )
        return Component.join(JoinConfiguration.newlines(), repr.toComponentLines())
    }
}