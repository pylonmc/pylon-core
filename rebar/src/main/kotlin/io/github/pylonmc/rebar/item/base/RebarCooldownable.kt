package io.github.pylonmc.rebar.item.base

import org.jetbrains.annotations.ApiStatus

/**
 * Implemented by other Rebar interfaces that may be affected by cooldowns.
 */
@ApiStatus.Internal
sealed interface RebarCooldownable {
    @Suppress("INAPPLICABLE_JVM_NAME") // tfw suppressing errors
    @get:JvmName("respectCooldown")
    val respectCooldown: Boolean
        get() = true
}