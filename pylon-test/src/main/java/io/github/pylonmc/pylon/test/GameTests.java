package io.github.pylonmc.pylon.test;

import io.github.pylonmc.pylon.core.block.BlockPosition;
import io.github.pylonmc.pylon.core.registry.PylonRegistry;
import io.github.pylonmc.pylon.core.test.GameTestConfig;
import io.github.pylonmc.pylon.core.test.GameTestFailException;
import io.github.pylonmc.pylon.test.gametest.GametestTest;
import io.github.pylonmc.pylon.test.gametest.PylonItemStackInterfaceTest;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

class GameTests {
    static void setUpGameTests() {
        PylonRegistry<GameTestConfig> registry = PylonRegistry.GAMETESTS;
        registry.register(GametestTest.get());
        registry.register(PylonItemStackInterfaceTest.get());
    }

    private static TestResult onComplete(GameTestConfig config, GameTestFailException e) {
        if (e != null) {
            TestAddon.instance().getLogger().log(Level.SEVERE, "Gametest %s failed!".formatted(config.getKey()), e);
        }
        return new TestResult(config.getKey(), e == null);
    }

    static @NotNull List<CompletableFuture<TestResult>> runGameTests() {
        int distance = 0;
        PylonRegistry<GameTestConfig> registry = PylonRegistry.GAMETESTS;
        List<CompletableFuture<TestResult>> futures = new ArrayList<>();
        for (GameTestConfig config : registry) {
            distance += config.getSize();
            futures.add(
                    config.launch(new BlockPosition(TestAddon.testWorld, distance, 1, 0))
                            .thenApply(e -> onComplete(config, e))
            );
            distance += 5 + config.getSize();
        }
        return futures;
    }
}
