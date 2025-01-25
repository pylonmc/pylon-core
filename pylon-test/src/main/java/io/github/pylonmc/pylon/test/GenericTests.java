package io.github.pylonmc.pylon.test;

import io.github.pylonmc.pylon.test.generictest.*;
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
        genericTests.add(new SerializerTestBlockPosition());
        genericTests.add(new SerializerTestChunkPosition());
        genericTests.add(new SerializerTestChar());
        genericTests.add(new SerializerTestSet());
        genericTests.add(new SerializerTestLocation());
        genericTests.add(new PylonPDCPrimitivesTest());
        genericTests.add(new PylonPDCSerializationTest());
    }

    private static TestResult onComplete(GenericTest test, Throwable e) {
        if (e != null) {
            TestAddon.instance().getLogger().log(Level.SEVERE, "Gametest %s failed!".formatted(test.getKey()), e);
        }
        return new TestResult(test.getKey(), e == null);
    }

    static @NotNull List<CompletableFuture<TestResult>> runGenericTests() {
        List<CompletableFuture<TestResult>> futures = new ArrayList<>();

        for (GenericTest test : genericTests) {
            CompletableFuture<TestResult> future = CompletableFuture.supplyAsync(() -> {
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
            }).thenApply(e -> onComplete(test, e));

            futures.add(future);
        }

        return futures;
    }
}
