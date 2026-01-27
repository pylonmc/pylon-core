package io.github.pylonmc.rebar.gametest

/**
 * Thrown when a [GameTest] fails.
 */
class GameTestFailException(test: GameTest, message: String, cause: Throwable? = null) :
    Exception("Gametest ${test.config.key} at ${test.center} failed: $message", cause)