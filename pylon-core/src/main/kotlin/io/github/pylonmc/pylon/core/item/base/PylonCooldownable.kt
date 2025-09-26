package io.github.pylonmc.pylon.core.item.base

import org.jetbrains.annotations.ApiStatus

/**
 * Implemented by other Pylon interfaces that may be affected by cooldowns.
 */
@ApiStatus.Internal
sealed interface PylonCooldownable {
    @Suppress("INAPPLICABLE_JVM_NAME") // tfw suppressing errors
    @get:JvmName("respectCooldown")
    val respectCooldown: Boolean
        get() = true
}