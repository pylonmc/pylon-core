package io.github.pylonmc.pylon.core.entity.base

interface PylonTickableEntity {
    /**
     * The interval at which the [tick] function is called. You should generally use [setTickInterval]
     * in your place constructor instead of overriding this.
     */
    val tickInterval: Int

    /**
     * Whether the [tick] function should be called asynchronously. You should generally use
     * [setAsync] in your place constructor instead of overriding this.
     */
    val isAsync: Boolean

    /**
     * Sets how often the [tick] function should be called (in Minecraft ticks)
     */
    fun setTickInterval(tickInterval: Int)

    /**
     * Sets whether the [tick] function should be called asynchronously.
     *
     * WARNING: Settings a entity to tick asynchronously could have unintended consequences.
     *
     * Only set this option if you understand what 'asynchronous' means, and note that you
     * cannot interact with the world asynchronously.
     */
    fun setAsync(isAsync: Boolean)

    /**
     * The function that should be called periodically.
     */
    fun tick(deltaSeconds: Double)
}