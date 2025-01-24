package io.github.pylonmc.pylon.test;

import io.github.pylonmc.pylon.core.block.BlockPosition;
import io.github.pylonmc.pylon.core.registry.PylonRegistries;
import io.github.pylonmc.pylon.core.registry.PylonRegistry;
import io.github.pylonmc.pylon.core.test.GameTestConfig;
import io.github.pylonmc.pylon.core.test.GameTestFailException;
import io.github.pylonmc.pylon.test.gametest.GametestTest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

class GameTests {
    static void setUpGameTests() {
        PylonRegistry<GameTestConfig> registry = PylonRegistries.GAMETESTS;
        registry.register(GametestTest.get());
    }

    private static void onComplete(GameTestConfig config, GameTestFailException e) {
        if (e != null) {
            Bukkit.broadcast(
                    Component.text("Gametest %s failed!".formatted(config.getKey()))
                            .color(NamedTextColor.RED)
            );
            TestAddon.instance().getLogger().log(Level.SEVERE, "Test failed", e);
        } else {
            Bukkit.broadcast(
                    Component.text("Gametest %s succeeded!".formatted(config.getKey()))
                            .color(NamedTextColor.GREEN)
            );
        }
    }

    static @NotNull List<CompletableFuture<Void>> runGameTests() {
        int distance = 0;
        PylonRegistry<GameTestConfig> registry = PylonRegistries.GAMETESTS;
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (GameTestConfig config : registry) {
            distance += config.getSize();
            futures.add(
                    config.launch(new BlockPosition(TestAddon.instance().testWorld, distance, 1, 0))
                            .thenAccept(e -> onComplete(config, e))
            );
            distance += 5 + config.getSize();
        }
        return futures;
    }
}
