package io.github.pylonmc.pylon.core.i18n

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.TranslationArgument
import net.kyori.adventure.text.VirtualComponentRenderer

class PylonArgument private constructor(val name: String, val value: Component) : VirtualComponentRenderer<Unit> {

    override fun apply(context: Unit): ComponentLike {
        return value
    }

    companion object {
        @JvmStatic
        fun of(name: String, value: Component): TranslationArgument {
            return TranslationArgument.component(Component.virtual(Unit::class.java, PylonArgument(name, value)))
        }

        @JvmStatic
        fun of(name: String, value: String): TranslationArgument {
            return of(name, Component.text(value))
        }

        @JvmStatic
        fun of(name: String, value: Int): TranslationArgument {
            return of(name, Component.text(value))
        }

        @JvmStatic
        fun of(name: String, value: Long): TranslationArgument {
            return of(name, Component.text(value))
        }

        @JvmStatic
        fun of(name: String, value: Double): TranslationArgument {
            return of(name, Component.text(value))
        }

        @JvmStatic
        fun of(name: String, value: Float): TranslationArgument {
            return of(name, Component.text(value))
        }

        @JvmStatic
        fun of(name: String, value: Boolean): TranslationArgument {
            return of(name, Component.text(value))
        }

        @JvmStatic
        fun of(name: String, value: Char): TranslationArgument {
            return of(name, Component.text(value))
        }
    }
}