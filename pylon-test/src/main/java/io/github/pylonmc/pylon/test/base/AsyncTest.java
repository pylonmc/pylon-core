package io.github.pylonmc.pylon.test.base;


import io.github.pylonmc.pylon.test.util.TestUtil;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;


/**
 * Executed asynchronously on the main thread (but not asynchronously to other tests)
 */
public abstract class AsyncTest implements Test {
    protected static int getTimeoutTicks() {
        return 30 * 20;
    }

    protected abstract void test();

    @Override
    public TestResult run() {
        Instant startTime = Instant.now();

        CompletableFuture<TestResult> future = TestUtil.runAsync(() -> {
            try {
                test();
            } catch (Throwable e) {
                return onComplete(e, startTime);
            }
            return onComplete(null, startTime);
        });

        TestUtil.runAsync(() -> {
            if (!future.isDone()) {
                future.complete(onComplete(new TimeoutException("Test timed out"), startTime));
            }
        }, getTimeoutTicks());

        return future.join();
    }
}