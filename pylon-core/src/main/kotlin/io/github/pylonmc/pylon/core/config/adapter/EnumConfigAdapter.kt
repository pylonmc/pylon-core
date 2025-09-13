package io.github.pylonmc.pylon.core.config.adapter

class EnumConfigAdapter<E : Enum<E>>(private val enumClass: Class<E>) : ConfigAdapter<E> {

    override val type = enumClass

    override fun convert(value: Any): E {
        val name = ConfigAdapter.STRING.convert(value)
        return enumClass.enumConstants.first { it.name.equals(name, ignoreCase = true) }
    }

    companion object {
        @JvmStatic
        fun <E : Enum<E>> from(enumClass: Class<E>): ConfigAdapter<E> {
            return EnumConfigAdapter(enumClass)
        }

        @JvmSynthetic
        inline fun <reified E : Enum<E>> from(): ConfigAdapter<E> {
            return from(E::class.java)
        }
    }
}