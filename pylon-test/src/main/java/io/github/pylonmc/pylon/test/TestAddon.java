package io.github.pylonmc.pylon.test;

import io.github.pylonmc.pylon.core.addon.PylonAddon;
import org.bukkit.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TestAddon extends JavaPlugin implements PylonAddon {
    private static TestAddon instance;
    public static World testWorld;

    @Override
    public void onEnable() {
        getLogger().info("Test addon enabled!");

        instance = this;

        testWorld = new WorldCreator("gametests")
                .generator(new BedrockWorldGenerator())
                .environment(World.Environment.NORMAL)
                .createWorld();
        assert testWorld != null; // shut up intellij // you can't make me, you fool. I will destroy you silly mortals.

        testWorld.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        testWorld.setGameRule(GameRule.DO_PATROL_SPAWNING, false);
        testWorld.setGameRule(GameRule.DO_TRADER_SPAWNING, false);

        GenericTests.setUpGenericTests();
        List<CompletableFuture<TestResult>>futures = GenericTests.runGenericTests();

        GameTests.setUpGameTests();
        futures.addAll(GameTests.runGameTests());

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                .thenApply(ignored -> futures.stream()
                        .map(CompletableFuture::join)
                        .toList())
                .thenAccept(results -> {
                    List<NamespacedKey> succeeded = results.stream()
                            .filter(TestResult::success)
                            .map(TestResult::key)
                            .toList();
                    List<NamespacedKey> failed = results.stream()
                            .filter(r -> !r.success())
                            .map(TestResult::key)
                            .toList();

                    Logger logger = TestAddon.instance().getLogger();

                    logger.info("[ ===== TEST SUMMARY ===== ]");
                    logger.info("%s/%s TESTS PASSED"
                            .formatted(succeeded.size(), succeeded.size() + failed.size()));

                    if (!failed.isEmpty()) {
                        String failedString = failed.stream()
                                .map(NamespacedKey::toString)
                                .collect(Collectors.joining(", "));
                        logger.info("FAILED: %s".formatted(failedString));

                        // Communicate back to the runServer task that the tests failed
                        File file = new File("tests-failed");
                        try {
                            file.createNewFile();
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    }

                    logger.info("Testing complete; shutting down server...");

                    if (!Boolean.parseBoolean(System.getenv("MANUAL_SHUTDOWN"))) {
                        Bukkit.shutdown();
                    }
                });
    }

    @Override
    public @NotNull JavaPlugin getJavaPlugin() {
        return this;
    }

    public static TestAddon instance() {
        return instance;
    }

    public static @NotNull NamespacedKey key(String key) {
        return new NamespacedKey(instance, key);
    }
}
