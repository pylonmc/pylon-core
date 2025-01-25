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
        genericTests.add(new PylonItemStackSimpleTest());
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
            Throwable result = null;
            try {
                test.run();
            } catch (Throwable e) {
                result = e;
            }

            if (result == null) {
                try {
                    test.cleanup();
                } catch (Throwable e) {
                    result = e;
                }
            }

            futures.add(CompletableFuture.completedFuture(onComplete(test, result)));
        }

        return futures;
    }
}
