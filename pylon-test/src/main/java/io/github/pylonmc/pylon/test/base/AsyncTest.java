package io.github.pylonmc.pylon.test.base;


import io.github.pylonmc.pylon.test.PylonTest;
import org.bukkit.Bukkit;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;


/**
 * Executed asynchronously on the main thread (but not asynchronously to other tests)
 */
public abstract class AsyncTest implements Test {
    private static final int TIMEOUT_TICKS = 30 * 20;

    protected abstract void test();

    @Override
    public TestResult run() {
        CompletableFuture<TestResult> future = new CompletableFuture<>();

        Bukkit.getScheduler().runTaskAsynchronously(PylonTest.instance(), () -> {
            try {
                test();
            } catch (Throwable e) {
                future.complete(onComplete(e));
                return;
            }
            future.complete(onComplete(null));
        });

        Bukkit.getScheduler().runTaskLater(PylonTest.instance(), () -> {
            if (!future.isDone()) {
                future.complete(onComplete(new TimeoutException("Test timed out")));
            }
        }, TIMEOUT_TICKS);

        return future.join();
    }
}