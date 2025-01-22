package io.github.pylonmc.pylon.test;

import io.github.pylonmc.pylon.core.addon.PylonAddon;
import io.github.pylonmc.pylon.core.registry.PylonRegistries;
import io.github.pylonmc.pylon.core.registry.PylonRegistry;
import io.github.pylonmc.pylon.core.registry.PyonRegistryKeys;
import io.github.pylonmc.pylon.core.test.GameTestConfig;
import io.github.pylonmc.pylon.core.test.GameTestFailException;
import kotlin.Unit;
import org.bukkit.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class TestAddon extends JavaPlugin implements PylonAddon {

    World testWorld;

    @Override
    public void onEnable() {
        getLogger().info("Test addon enabled!");

        testWorld = new WorldCreator("gametests")
                .generator(new BedrockWorldGenerator())
                .environment(World.Environment.NORMAL)
                .createWorld();
        assert testWorld != null; // shut up intellij

        testWorld.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        testWorld.setGameRule(GameRule.DO_PATROL_SPAWNING, false);
        testWorld.setGameRule(GameRule.DO_TRADER_SPAWNING, false);

        setUpGameTests();

        runGameTests();
    }

    private void setUpGameTests() {
        PylonRegistry<GameTestConfig> registry = PylonRegistries.getRegistry(PyonRegistryKeys.GAMETESTS);
        registry.register(
                new GameTestConfig.Builder(new NamespacedKey(this, "test"))
                        .size(2)
                        .setUp((test) -> {
                            test.offset(0, 0, 0).getBlock().setType(Material.DIAMOND_BLOCK);
                            return Unit.INSTANCE;
                        })
                        .build()
        );
    }

    private void runGameTests() {
        PylonRegistry<GameTestConfig> registry = PylonRegistries.getRegistry(PyonRegistryKeys.GAMETESTS);
        List<CompletableFuture<GameTestFailException>> futures = new ArrayList<>();
        for (GameTestConfig config : registry) {
            if (config.isParallelCapable()) {
                futures.add(config.launch(testWorld));
            }
        }
        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                .thenRun(() -> {
                    for (CompletableFuture<@Nullable GameTestFailException> future : futures) {
                        reportGameTestFailure(future.join());
                    }
                    CompletableFuture<GameTestFailException> syncFuture = new CompletableFuture<>();
                    for (GameTestConfig config : registry) {
                        if (!config.isParallelCapable()) {
                            syncFuture = syncFuture.thenCompose((e) -> {
                                reportGameTestFailure(e);
                                return config.launch(testWorld);
                            });
                        }
                    }
                    syncFuture.thenRun(() -> getLogger().info("All tests complete!"));
                    if (!Boolean.parseBoolean(System.getenv("MANUAL_SHUTDOWN"))) {
                        syncFuture.thenRun(Bukkit::shutdown);
                    }
                });
    }

    private void reportGameTestFailure(@Nullable GameTestFailException exception) {
        if (exception != null) {
            getLogger().log(Level.SEVERE, "Test failed", exception);
        }
    }

    @Override
    public @NotNull JavaPlugin getJavaPlugin() {
        return this;
    }
}
