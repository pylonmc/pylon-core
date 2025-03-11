package io.github.pylonmc.pylon.test.base;

import io.github.pylonmc.pylon.core.test.GameTestConfig;
import io.github.pylonmc.pylon.core.test.GameTestFailException;
import io.github.pylonmc.pylon.core.util.BlockPosition;
import io.github.pylonmc.pylon.test.PylonTest;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

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
        // Nested futures because we need to run launch on the main thread
        CompletableFuture<CompletableFuture<GameTestFailException>> testFuture = new CompletableFuture<>();

        Bukkit.getScheduler().runTask(PylonTest.instance(), () -> {
            distance += 5 + config.getSize();
            testFuture.complete(config.launch(new BlockPosition(PylonTest.testWorld, distance, 1, 0)));
            distance += 5 + config.getSize();
        });

        return onComplete(testFuture.join().join());
    }
}
