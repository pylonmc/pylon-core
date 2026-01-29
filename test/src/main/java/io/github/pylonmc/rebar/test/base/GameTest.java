package io.github.pylonmc.rebar.test.base;

import io.github.pylonmc.rebar.gametest.GameTestConfig;
import io.github.pylonmc.rebar.gametest.GameTestFailException;
import io.github.pylonmc.rebar.test.RebarTest;
import io.github.pylonmc.rebar.test.util.TestUtil;
import io.github.pylonmc.rebar.util.position.BlockPosition;
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
                    = config.launch(new BlockPosition(RebarTest.testWorld, distance, 1, 0));
            distance += 5 + config.getSize();
            return completedFuture;
        }).join().join();

        return onComplete(e, startTime);
    }
}
