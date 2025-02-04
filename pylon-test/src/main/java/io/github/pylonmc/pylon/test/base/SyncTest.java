package io.github.pylonmc.pylon.test.base;

import io.github.pylonmc.pylon.test.PylonTest;
import org.bukkit.Bukkit;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;


public abstract class SyncTest implements Test {
    protected int getTimeoutTicks() {
        return 30 * 20;
    }

    protected abstract void test();

    @Override
    public TestResult run() {
        CompletableFuture<TestResult> future = new CompletableFuture<>();

        Bukkit.getScheduler().runTask(PylonTest.instance(), () -> {
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
        }, getTimeoutTicks());

        return future.join();
    }
}