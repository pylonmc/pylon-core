package io.github.pylonmc.pylon.core.config.adapter

import org.bukkit.Tag

object TagConfigAdapter : ConfigAdapter<Tag<*>> {
    override val type = Tag::class.java

    override fun convert(value: Any): Tag<*> {
        val tag = ConfigAdapter.STRING.convert(value).removePrefix("#")

    }
}