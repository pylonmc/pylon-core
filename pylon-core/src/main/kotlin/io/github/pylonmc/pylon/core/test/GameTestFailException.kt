package io.github.pylonmc.pylon.core.test

class GameTestFailException(test: GameTest, message: String, cause: Throwable? = null) :
    Exception("Gametest ${test.config.key} at ${test.center} failed: $message", cause)