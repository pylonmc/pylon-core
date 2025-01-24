package io.github.pylonmc.pylon.test;

import io.github.pylonmc.pylon.core.addon.PylonAddon;
import io.github.pylonmc.pylon.core.block.BlockPosition;
import io.github.pylonmc.pylon.core.registry.PylonRegistries;
import io.github.pylonmc.pylon.core.registry.PylonRegistry;
import io.github.pylonmc.pylon.core.test.GameTestConfig;
import io.github.pylonmc.pylon.core.test.GameTestFailException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fox;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

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
        PylonRegistry<GameTestConfig> registry = PylonRegistries.GAMETESTS;
        registry.register(
                new GameTestConfig.Builder(new NamespacedKey(this, "test"))
                        .size(1)
                        .setUp((test) -> {
                            test.getWorld().spawn(test.location(1.5, 0, 0), Fox.class);
                            test.getWorld().spawn(test.location(1.5, 0, 1), Chicken.class);
                            test.succeedWhen(() -> !test.entityInBounds((entity) -> entity.getType() == EntityType.CHICKEN));
                        })
                        .build()
        );
    }

    private void runGameTests() {
        int distance = 0;
        PylonRegistry<GameTestConfig> registry = PylonRegistries.GAMETESTS;
        List<CompletableFuture<GameTestFailException>> futures = new ArrayList<>();
        for (GameTestConfig config : registry) {
            distance += config.getSize();
            futures.add(
                    config.launch(new BlockPosition(testWorld, distance, 1, 0)).thenApply(
                            (e) -> {
                                if (e != null) {
                                    Bukkit.broadcast(
                                            Component.text("Test %s failed!".formatted(config.getKey()))
                                                    .color(NamedTextColor.RED)
                                    );
                                    getLogger().log(Level.SEVERE, "Test failed", e);
                                } else {
                                    Bukkit.broadcast(
                                            Component.text("Test %s succeeded!".formatted(config.getKey()))
                                                    .color(NamedTextColor.GREEN)
                                    );
                                }
                                return e;
                            }
                    )
            );
            distance += 5 + config.getSize();
        }
        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                .thenRun(() -> {
                    getLogger().info("All tests complete!");
                    if (!Boolean.parseBoolean(System.getenv("MANUAL_SHUTDOWN"))) {
                        Bukkit.shutdown();
                    }
                });
    }

    @Override
    public @NotNull JavaPlugin getJavaPlugin() {
        return this;
    }
}
