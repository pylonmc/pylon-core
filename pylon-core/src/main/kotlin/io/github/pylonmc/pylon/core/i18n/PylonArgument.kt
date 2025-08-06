package io.github.pylonmc.pylon.core.i18n

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

        /**
         * Attaches arguments to a Pylon translation key, making sure they are replaced
         * correctly when the translation is rendered.
         */
        @JvmStatic
        @JvmName("attachArguments")
        fun Component.attachPylonArguments(args: List<PylonArgument>): Component {
            if (args.isEmpty()) return this
            var result = this
            if (this is TranslatableComponent) {
                result = this.arguments(args)
            }
            return result.children(result.children().map { it.attachPylonArguments(args) })
        }

        /**
         * Attaches arguments to a Pylon translation key, making sure they are replaced
         * correctly when the translation is rendered.
         */
        @JvmStatic
        @JvmName("attachArguments")
        fun Component.attachPylonArguments(vararg args: PylonArgument): Component = attachPylonArguments(args.toList())
    }
}