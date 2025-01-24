package io.github.pylonmc.pylon.test;

import io.github.pylonmc.pylon.test.generictest.SerializerTestNamespacedKey;
import io.github.pylonmc.pylon.test.generictest.SerializerTestUUID;
import io.github.pylonmc.pylon.test.generictest.SerializerTestVector;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

class GenericTests {
    private final static List<GenericTest> genericTests = new ArrayList<>();

    static void setUpGenericTests() {
        genericTests.add(new SerializerTestUUID());
        genericTests.add(new SerializerTestNamespacedKey());
        genericTests.add(new SerializerTestVector());
    }

    private static void onComplete(GenericTest test, Throwable e) {
        if (e != null) {
            Bukkit.broadcast(
                    Component.text("Generic test %s failed!".formatted(test.getKey()))
                            .color(NamedTextColor.RED)
            );
            TestAddon.instance().getLogger().log(Level.SEVERE, "Generic test failed", e);
        } else {
            Bukkit.broadcast(
                    Component.text("Generic test %s succeeded!".formatted(test.getKey()))
                            .color(NamedTextColor.GREEN)
            );
        }
    }

    static @NotNull List<CompletableFuture<Void>> runGenericTests() {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (GenericTest test : genericTests) {
            CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
                try {
                    test.run();
                } catch (Throwable e) {
                    return e;
                }

                try {
                    test.cleanup();
                } catch (Throwable e) {
                    return e;
                }

                return null;
            }).thenAccept(e -> onComplete(test, e));

            futures.add(future);
        }

        return futures;
    }
}
