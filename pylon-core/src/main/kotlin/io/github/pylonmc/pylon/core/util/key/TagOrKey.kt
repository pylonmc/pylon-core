package io.github.pylonmc.pylon.core.util.key

import org.bukkit.Keyed
import org.bukkit.NamespacedKey

sealed interface TagOrKey {

    fun matches(key: NamespacedKey): Boolean

    data class Tag(val tag: Set<NamespacedKey>) : TagOrKey {
        constructor(vararg tag: NamespacedKey) : this(tag.toSet())
        constructor(vararg tag: Keyed) : this(tag.map { it.key }.toSet())
        constructor(bukkitTag: org.bukkit.Tag<*>) : this(bukkitTag.values.map { it.key }.toSet())

        override fun matches(key: NamespacedKey): Boolean {
            return tag.any { it == key }
        }
    }

    data class Key(val key: NamespacedKey) : TagOrKey {
        constructor(keyed: Keyed) : this(keyed.key)

        override fun matches(key: NamespacedKey): Boolean {
            return this.key == key
        }
    }
}