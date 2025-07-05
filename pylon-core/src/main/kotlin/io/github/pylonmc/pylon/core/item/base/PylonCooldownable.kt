package io.github.pylonmc.pylon.core.item.base

/**
 * To be extended by interfaces that can be affected by cooldowns.
 * Not to be directly implemented by items.
 */
interface PylonCooldownable {
    @Suppress("INAPPLICABLE_JVM_NAME") // tfw suppressing errors
    @get:JvmName("respectCooldown")
    val respectCooldown: Boolean
        get() = true
}