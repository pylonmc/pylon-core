package io.github.pylonmc.pylon.core.item.base

import org.jetbrains.annotations.ApiStatus

/**
 * Implemented by interfaces that may be affected by cooldowns. You should not
 * implement this interface on an item yourself.
 */
@ApiStatus.Internal
sealed interface PylonCooldownable {
    @Suppress("INAPPLICABLE_JVM_NAME") // tfw suppressing errors
    @get:JvmName("respectCooldown")
    val respectCooldown: Boolean
        get() = true
}