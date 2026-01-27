package io.github.pylonmc.rebar.test.base;

import io.github.pylonmc.rebar.test.util.TestUtil;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;


public abstract class SyncTest implements Test {
    protected static int getTimeoutTicks() {
        return 30 * 20;
    }

    protected abstract void test();

    @Override
    public TestResult run() {
        Instant startTime = Instant.now();

        CompletableFuture<TestResult> future = TestUtil.runSync(() -> {
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