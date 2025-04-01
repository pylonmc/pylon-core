package io.github.pylonmc.pylon.test.base;

import io.github.pylonmc.pylon.test.util.TestUtil;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;


public abstract class SyncTest implements Test {
    protected static int getTimeoutTicks() {
        return 30 * 20;
    }

    protected abstract void test();

    @Override
    public TestResult run() {
        CompletableFuture<TestResult> future = TestUtil.runSync(() -> {
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