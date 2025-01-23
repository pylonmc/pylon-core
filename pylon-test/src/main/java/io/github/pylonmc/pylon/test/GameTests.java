package io.github.pylonmc.pylon.test;

import io.github.pylonmc.pylon.core.block.BlockPosition;
import io.github.pylonmc.pylon.core.registry.PylonRegistries;
import io.github.pylonmc.pylon.core.registry.PylonRegistry;
import io.github.pylonmc.pylon.core.registry.PyonRegistryKeys;
import io.github.pylonmc.pylon.core.test.GameTestConfig;
import io.github.pylonmc.pylon.core.test.GameTestFailException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fox;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

class GameTests {
    static void setUpGameTests(TestAddon addon) {
        PylonRegistry<GameTestConfig> registry = PylonRegistries.getRegistry(PyonRegistryKeys.GAMETESTS);
        registry.register(
                new GameTestConfig.Builder(new NamespacedKey(addon, "test"))
                        .size(1)
                        .setUp((test) -> {
                            test.getWorld().spawn(test.location(1.5, 0, 0), Fox.class);
                            test.getWorld().spawn(test.location(1.5, 0, 1), Chicken.class);
                            test.succeedWhen(() -> !test.entityInBounds((entity) -> entity.getType() == EntityType.CHICKEN));
                        })
                        .build()
        );
    }

    static void runGameTests(TestAddon addon) {
        int distance = 0;
        PylonRegistry<GameTestConfig> registry = PylonRegistries.getRegistry(PyonRegistryKeys.GAMETESTS);
        List<CompletableFuture<GameTestFailException>> futures = new ArrayList<>();
        for (GameTestConfig config : registry) {
            distance += config.getSize();
            futures.add(
                    config.launch(new BlockPosition(addon.testWorld, distance, 1, 0)).thenApply(
                            (e) -> {
                                if (e != null) {
                                    Bukkit.broadcast(
                                            Component.text("Test %s failed!".formatted(config.getKey()))
                                                    .color(NamedTextColor.RED)
                                    );
                                    addon.getLogger().log(Level.SEVERE, "Test failed", e);
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
                    addon.getLogger().info("All tests complete!");
                    if (!Boolean.parseBoolean(System.getenv("MANUAL_SHUTDOWN"))) {
                        Bukkit.shutdown();
                    }
                });
    }
}
