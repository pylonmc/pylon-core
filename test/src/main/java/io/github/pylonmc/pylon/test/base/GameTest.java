package io.github.pylonmc.pylon.test.base;

import io.github.pylonmc.pylon.core.gametest.GameTestConfig;
import io.github.pylonmc.pylon.core.gametest.GameTestFailException;
import io.github.pylonmc.pylon.core.util.position.BlockPosition;
import io.github.pylonmc.pylon.test.PylonTest;
import io.github.pylonmc.pylon.test.util.TestUtil;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;


public class GameTest implements Test {
    private static int distance;

    private final GameTestConfig config;

    public GameTest(@NotNull GameTestConfig config) {
        this.config = config;
        config.register();
    }

    @Override
    public NamespacedKey getKey() {
        return config.getKey();
    }

    @Override
    public TestResult run() {
        Instant startTime = Instant.now();

        GameTestFailException e = TestUtil.runSync(() -> {
            distance += 5 + config.getSize();
            CompletableFuture<GameTestFailException> completedFuture
                    = config.launch(new BlockPosition(PylonTest.testWorld, distance, 1, 0));
            distance += 5 + config.getSize();
            return completedFuture;
        }).join().join();

        return onComplete(e, startTime);
    }
}
