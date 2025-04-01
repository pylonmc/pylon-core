package io.github.pylonmc.pylon.test.base;


import io.github.pylonmc.pylon.test.PylonTest;
import io.github.pylonmc.pylon.test.util.TestUtil;
import org.bukkit.Bukkit;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;


/**
 * Executed asynchronously on the main thread (but not asynchronously to other tests)
 */
public abstract class AsyncTest implements Test {
    protected int getTimeoutTicks() {
        return 30 * 20;
    }

    protected abstract void test();

    @Override
    public TestResult run() {
        CompletableFuture<TestResult> future = TestUtil.runAsync(() -> {
            try {
                test();
            } catch (Throwable e) {
                return onComplete(e);
            }
            return onComplete(null);
        });

        TestUtil.runAsync(() -> {
            if (!future.isDone()) {
                future.complete(onComplete(new TimeoutException("Test timed out")));
            }
        }, getTimeoutTicks());

        return future.join();
    }
}