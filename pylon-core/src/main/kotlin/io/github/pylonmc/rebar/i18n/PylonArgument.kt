package io.github.pylonmc.rebar.i18n

import net.kyori.adventure.text.*

/**
 * A [TranslationArgument] only to be used when translating Pylon keys
 */
class PylonArgument private constructor(val name: String, val value: ComponentLike) :
    VirtualComponentRenderer<Unit>, TranslationArgumentLike {

    override fun apply(context: Unit): ComponentLike {
        return value
    }

    override fun asTranslationArgument(): TranslationArgument {
        return TranslationArgument.component(
            Component.virtual(
                Unit::class.java,
                this
            ).append(value) // Append the value for comparison purposes, it'll get thrown out anyway
        )
    }

    companion object {
        @JvmStatic
        fun of(name: String, value: ComponentLike): PylonArgument {
            return PylonArgument(name, value)
        }

        @JvmStatic
        fun of(name: String, value: String): PylonArgument {
            return of(name, Component.text(value))
        }

        @JvmStatic
        fun of(name: String, value: Int): PylonArgument {
            return of(name, Component.text(value))
        }

        @JvmStatic
        fun of(name: String, value: Long): PylonArgument {
            return of(name, Component.text(value))
        }

        @JvmStatic
        fun of(name: String, value: Double): PylonArgument {
            return of(name, Component.text(value))
        }

        @JvmStatic
        fun of(name: String, value: Float): PylonArgument {
            return of(name, Component.text(value))
        }

        @JvmStatic
        fun of(name: String, value: Boolean): PylonArgument {
            return of(name, Component.text(value))
        }

        @JvmStatic
        fun of(name: String, value: Char): PylonArgument {
            return of(name, Component.text(value))
        }
    }
}